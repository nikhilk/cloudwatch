# capture standard output
import sys
import StringIO
import contextlib

@contextlib.contextmanager
def stdoutIO(stdout=None):
    old = sys.stdout
    if stdout is None:
        stdout = StringIO.StringIO()
    sys.stdout = stdout
    yield stdout
    sys.stdout = old



class metric_reader():
    def __init__(self):
        pass

    # read a notebook, extract code, replace magic with appropriate calls and return code as a string
    @staticmethod
    def ipynb_to_code(bucket, notebook):
        import gcp
        import gcp.storage as storage
        bucket = storage.bucket('cloud-androidwear-notebooks')
        notebook_json = bucket.item("App Metrics.ipynb").read_from()
        import json as json_parser
        notebook = json_parser.loads(notebook_json)
        cells = notebook["worksheets"][0]["cells"]
        code_inputs = [cell["input"] for cell in cells if cell["cell_type"] == "code"]
        
        # Handle bq magic
        import re
        # output the bq_sql magic code
        def muggle_bq_sql(symbol, lines):
            return [symbol + ' = gcp.bigquery.query(gcp.bigquery.sql("""\n'] + lines + ['""", **locals()))'];

        # TODO: this would be a good place to handle all the magic
        def muggle(lines):
            # if we've got nothing, bail
            if lines == []: return lines

            # if we've got some bg_sql magic, handle it
            regex = re.compile("^\s*%%bq_sql\s*(?P<symbol>\S*)\s*$", re.IGNORECASE)
            match = regex.match(lines[0])
            if match:
                # if we've got bq_sql w/o a symbol, do nothing
                symbol = match.groups("symbol")[0]
                if symbol == "": return []

                # if we've got a symbol, muggle the bq_sql code
                else: return muggle_bq_sql(symbol, lines[1:])
            # otherwise, let it through
            else: return lines

        muggle_code_inputs = map(muggle, code_inputs)
        flat_code = [item for sublist in muggle_code_inputs for item in sublist]
        imports = ["gcp.bigquery"]
        code = '\n'.join(["import " + i for i in imports]) + '\n' + '\n'.join(flat_code)
        return code


    # Execute the string code - capture stdio
    @staticmethod
    def execute_code(code):   
        # TODO: pass input via exec_locals
        exec_locals = {}
        exec_globals = {}
        
        with stdoutIO() as exec_output:
            exec(code, exec_globals, exec_locals)
        # standard output available in exec_output
        exec_output.getvalue()
        # values set in the executed code will be in exec_globals
        
        return(exec_locals["metric_results"])


    # Driver
    @staticmethod
    def read(bucket = 'cloud-androidwear-notebooks', notebook = 'App Metrics.ipynb'):
        code = metric_reader.ipynb_to_code(bucket, notebook)
        return (metric_reader.execute_code(code))