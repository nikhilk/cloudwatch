// WatchFace.java
//

package google.com.cloudwatch;

import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.support.v4.util.*;
import android.text.format.*;
import android.view.*;

import java.text.*;
import java.util.*;

public class WatchFace extends View {

  private Calendar _calendar;
  private Time _time;
  private SimpleDateFormat _dayFormat;
  private SimpleDateFormat _dateFormat;

  private Bitmap _background;
  private Paint _backgroundPaint;
  private Paint _headingTextPaint;
  private Paint _valueTextPaint;

  private Path _metricHeadingPath;
  private Path _metricValuePath;

  private Path _dateHeadingPath;
  private Path _dateValuePath;

  private Paint _hourHandPaint;
  private Paint _minuteHandPaint;
  private Paint _secondHandPaint;

  private Paint _timeSeriesPaint;
  private Paint _currentValuePaint;

  private CircularArray<Float> _values;
  private Random _valueGenerator;

  public WatchFace(Context context) {
    super(context);

    _time = new Time(TimeZone.getDefault().getID());
    _calendar = Calendar.getInstance();
    _dayFormat = new SimpleDateFormat("cccc");
    _dateFormat = new SimpleDateFormat("MMM d");

    _values = new CircularArray<Float>(100);
    _valueGenerator = new Random();

    for (int i = 0; i < 50; i++) {
      _values.addLast(_valueGenerator.nextInt(40) - 20f);
    }

    _headingTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    _headingTextPaint.setAntiAlias(true);
    _headingTextPaint.setStrokeWidth(1);
    _headingTextPaint.setStyle(Paint.Style.FILL);
    _headingTextPaint.setColor(0x80ffffff);
    _headingTextPaint.setTextAlign(Paint.Align.CENTER);
    _headingTextPaint.setTextSize(12f);
    _headingTextPaint.setFakeBoldText(true);

    _valueTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    _valueTextPaint.setAntiAlias(true);
    _valueTextPaint.setStrokeWidth(1);
    _valueTextPaint.setStyle(Paint.Style.FILL);
    _valueTextPaint.setColor(0xffffffff);
    _valueTextPaint.setTextAlign(Paint.Align.CENTER);
    _valueTextPaint.setTextSize(14f);
    _valueTextPaint.setFakeBoldText(true);

    _background = createBackground(context);
    _backgroundPaint = new Paint();
    _backgroundPaint.setFilterBitmap(true);

    _metricHeadingPath = new Path();
    _metricHeadingPath.moveTo(50, 110);
    _metricHeadingPath.lineTo(150, 110);

    _metricValuePath = new Path();
    _metricValuePath.moveTo(50, 130);
    _metricValuePath.lineTo(150, 130);

    _dateHeadingPath = new Path();
    _dateHeadingPath.moveTo(170, 110);
    _dateHeadingPath.lineTo(270, 110);

    _dateValuePath = new Path();
    _dateValuePath.moveTo(170, 130);
    _dateValuePath.lineTo(270, 130);

    _hourHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    _hourHandPaint.setAntiAlias(true);
    _hourHandPaint.setStrokeWidth(3);
    _hourHandPaint.setStrokeCap(Paint.Cap.SQUARE);
    _hourHandPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    _hourHandPaint.setColor(0xffffffff);
    _hourHandPaint.setShadowLayer(4, 0, 0, 0xff000000);

    _minuteHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    _minuteHandPaint.setAntiAlias(true);
    _minuteHandPaint.setStrokeWidth(2);
    _minuteHandPaint.setStrokeCap(Paint.Cap.SQUARE);
    _minuteHandPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    _minuteHandPaint.setColor(0xffffffff);
    _minuteHandPaint.setShadowLayer(4, 0, 0, 0xff000000);

    _secondHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    _secondHandPaint.setAntiAlias(true);
    _secondHandPaint.setStrokeWidth(2);
    _secondHandPaint.setStrokeCap(Paint.Cap.SQUARE);
    _secondHandPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    _secondHandPaint.setColor(0xff808080);
    _secondHandPaint.setShadowLayer(4, 0, 0, 0xff000000);

    _timeSeriesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    _timeSeriesPaint.setAntiAlias(true);
    _timeSeriesPaint.setStrokeWidth(1);
    _timeSeriesPaint.setStrokeCap(Paint.Cap.ROUND);
    _timeSeriesPaint.setStyle(Paint.Style.STROKE);
    _timeSeriesPaint.setColor(0xb0ffffff);

    _currentValuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    _currentValuePaint.setAntiAlias(true);
    _currentValuePaint.setStrokeWidth(1);
    _currentValuePaint.setStrokeCap(Paint.Cap.ROUND);
    _currentValuePaint.setStyle(Paint.Style.FILL_AND_STROKE);
    _currentValuePaint.setColor(0xffffffff);

    final Handler h = new Handler() {
      @Override
      public void handleMessage(Message msg) {
        updateTime();
      }
    };

    Timer timer = new Timer();
    timer.scheduleAtFixedRate(new TimerTask()
    {
      @Override
      public void run()
      {
        h.sendEmptyMessage(0);
      }
    }, 0, 1000);
  }

  private Bitmap createBackground(Context context) {
    Bitmap bitmap = Bitmap.createBitmap(320, 320, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);

    Paint backgroundPaint = new Paint();
    backgroundPaint.setColor(0xff242424);
    backgroundPaint.setStyle(Paint.Style.FILL);

    Paint blackFill = new Paint(Paint.ANTI_ALIAS_FLAG);
    blackFill.setAntiAlias(true);
    blackFill.setStyle(Paint.Style.FILL);
    blackFill.setColor(0xff000000);

    Paint blackStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    blackStroke.setAntiAlias(true);
    blackStroke.setStyle(Paint.Style.STROKE);
    blackStroke.setStrokeWidth(12);
    blackStroke.setStrokeCap(Paint.Cap.ROUND);
    blackStroke.setColor(0xff242424);
    blackStroke.setShadowLayer(4, 0, 0, 0xff000000);

    Paint brandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    brandPaint.setAntiAlias(true);
    brandPaint.setStrokeWidth(1);
    brandPaint.setStyle(Paint.Style.FILL);
    brandPaint.setColor(0xffffffff);
    brandPaint.setTextAlign(Paint.Align.CENTER);
    brandPaint.setTextSize(11f);

    Path brandPath = new Path();
    brandPath.moveTo(75, 60);
    brandPath.lineTo(245, 60);
    // brandPath.addArc(new RectF(40, 40, 280, 280), -180, 180);

    Drawable brandLogo = context.getResources().getDrawable(R.drawable.cloud);
    brandLogo.setBounds(new Rect(133, 136, 187, 184));

    Paint tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    tickPaint.setStrokeWidth(2);
    tickPaint.setStrokeCap(Paint.Cap.BUTT);
    tickPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    tickPaint.setColor(0xffffffff);
    tickPaint.setShadowLayer(4, 0, 0, 0xff000000);

    Paint bigTickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    bigTickPaint.setStrokeWidth(4);
    bigTickPaint.setStrokeCap(Paint.Cap.BUTT);
    bigTickPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    bigTickPaint.setColor(0xffffffff);
    bigTickPaint.setShadowLayer(4, 0, 0, 0xff000000);

    Paint smallTickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    smallTickPaint.setStrokeWidth(1);
    smallTickPaint.setStrokeCap(Paint.Cap.ROUND);
    smallTickPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    smallTickPaint.setColor(0xffffffff);

    // Background
    canvas.drawRect(0, 0, 320, 320, backgroundPaint);

    // Branding
    canvas.drawCircle(160, 160, 40, blackFill);
    canvas.drawTextOnPath("Google Cloud Watch", brandPath, 0, 0, brandPaint);
    brandLogo.draw(canvas);

    // Hour markers
    canvas.save(Canvas.MATRIX_SAVE_FLAG);
    for (int i = 0; i < 12; i++) {
      Paint paint = i % 3 == 0 ? bigTickPaint : tickPaint;
      int tickSize = i % 3 == 0 ? 20 : 10;

      canvas.drawLine(300 - tickSize, 160, 300, 160, paint);
      canvas.rotate(30f, 160f, 160f);
    }
    canvas.restore();

    // Small ticks between hour markers
    canvas.save(Canvas.MATRIX_SAVE_FLAG);
    for (int i = 0; i < 60; i++) {
      if (i % 5 != 0) {
        canvas.drawCircle(300, 160, 0.25f, smallTickPaint);
      }
      canvas.rotate(6f, 160f, 160f);
    }
    canvas.restore();

    canvas.drawCircle(160, 160, 2f, smallTickPaint);

    /*
    // Metric arc
    canvas.save(Canvas.MATRIX_SAVE_FLAG);
    canvas.rotate(135, 160, 160);
    canvas.drawArc(new RectF(8, 8, 312, 312), 0, 270, false, blackStroke);
    canvas.restore();
    */

    return bitmap;
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    canvas.drawBitmap(_background, 0, 0, _backgroundPaint);

    canvas.drawTextOnPath("LATENCY", _metricHeadingPath, 0, 0, _headingTextPaint);
    canvas.drawTextOnPath("500ms", _metricValuePath, 0, 0, _valueTextPaint);

    canvas.drawTextOnPath(_dayFormat.format(_calendar.getTime()).toUpperCase(),
                          _dateHeadingPath, 0, 0, _headingTextPaint);
    canvas.drawTextOnPath(_dateFormat.format(_calendar.getTime()),
                          _dateValuePath, 0, 0, _valueTextPaint);

    Path path = new Path();
    path.moveTo(75f, 230f);

    float x = 0;
    float y = 0;
    for (int i = 0; i < _values.size(); i++) {
      x = i * 1.7f + 75;
      y = _values.get(i) + 230;
      path.lineTo(x, y);
    }
    canvas.drawPath(path, _timeSeriesPaint);
    canvas.drawCircle(x, y, 1.25f, _currentValuePaint);

    canvas.save(Canvas.MATRIX_SAVE_FLAG);
    canvas.rotate(30 * (_time.hour + _time.minute / 60f) - 90, 160, 160);
    canvas.drawLine(165, 160, 225, 160, _hourHandPaint);
    canvas.restore();

    canvas.save(Canvas.MATRIX_SAVE_FLAG);
    canvas.rotate(6 * (_time.minute + _time.second / 60f) - 90, 160, 160);
    canvas.drawLine(165, 160, 270, 160, _minuteHandPaint);
    canvas.restore();

    canvas.save(Canvas.MATRIX_SAVE_FLAG);
    canvas.rotate(6 * _time.second - 90, 160, 160);
    canvas.drawLine(165, 160, 280, 160, _secondHandPaint);
    canvas.restore();
  }

  public void updateMetrics(Map<String, Object> metricData) {
    invalidate();
  }

  public void updateTime() {
    _time.setToNow();

    if (_values.size() == 100) {
      _values.popFirst();
    }
    _values.addLast(_valueGenerator.nextInt(40) - 20f);

    invalidate();
  }
}
