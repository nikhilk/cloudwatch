// WatchFace.java
//

package google.com.cloudwatch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public final class WatchFace extends View {

  private Calendar _calendar;
  private Time _time;
  private SimpleDateFormat _dayFormat;
  private SimpleDateFormat _dateFormat;

  private Bitmap _background;
  private Paint _backgroundPaint;

  private Paint _hourHandPaint;
  private Paint _minuteHandPaint;
  private Paint _secondHandPaint;

  private Paint _dayNamePaint;
  private Paint _datePaint;
  private Path _dateHeadingPath;
  private Path _dateValuePath;

  private Paint _metricNamePaint;
  private Paint _metricValuePaint;
  private Path _metricHeadingPath;
  private Path _metricValuePath;

  private Paint _timeSeriesPaint;
  private Paint _currentValuePaint;
  private Path _timeSeriesPath;

  private RectF _metricArcBounds;
  private Paint _metricOKPaint;
  private Paint _metricWarningPaint;
  private Paint _metricErrorPaint;

  private String _metricName;
  private String _metricUnit;
  private float _metricMaxValue;
  private ArrayList<Float> _values;

  public WatchFace(Context context) {
    super(context);

    _time = new Time(TimeZone.getDefault().getID());
    _calendar = Calendar.getInstance();
    _dayFormat = new SimpleDateFormat("cccc");
    _dateFormat = new SimpleDateFormat("MMM d");

    _metricName = "Latency";
    _metricUnit = "ms";
    _metricMaxValue = 35;
    _values = new ArrayList<Float>(100);

    createDrawingObjects(context);
    createTimer();
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

    Paint metricPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    metricPaint.setAntiAlias(true);
    metricPaint.setStrokeWidth(7);
    metricPaint.setStrokeCap(Paint.Cap.BUTT);
    metricPaint.setStyle(Paint.Style.STROKE);
    metricPaint.setColor(0xff4d4848);
    metricPaint.setShadowLayer(4, 0, 0, 0xff484848);

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

    // Center dot
    canvas.drawCircle(160, 160, 2f, smallTickPaint);

    // Metric ring
    canvas.save(Canvas.MATRIX_SAVE_FLAG);
    canvas.rotate(135, 160, 160);
    canvas.drawArc(_metricArcBounds, 0, 270, false, metricPaint);
    canvas.restore();

    return bitmap;
  }

  private void createDrawingObjects(Context context) {
    _metricHeadingPath = new Path();
    _metricHeadingPath.moveTo(55, 110);
    _metricHeadingPath.lineTo(160, 110);

    _metricValuePath = new Path();
    _metricValuePath.moveTo(55, 130);
    _metricValuePath.lineTo(160, 130);

    _dateHeadingPath = new Path();
    _dateHeadingPath.moveTo(160, 110);
    _dateHeadingPath.lineTo(265, 110);

    _dateValuePath = new Path();
    _dateValuePath.moveTo(160, 130);
    _dateValuePath.lineTo(265, 130);

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

    _metricNamePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    _metricNamePaint.setAntiAlias(true);
    _metricNamePaint.setStrokeWidth(1);
    _metricNamePaint.setStyle(Paint.Style.FILL);
    _metricNamePaint.setColor(0x80ffffff);
    _metricNamePaint.setTextAlign(Paint.Align.LEFT);
    _metricNamePaint.setTextSize(12f);
    _metricNamePaint.setFakeBoldText(true);

    _metricValuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    _metricValuePaint.setAntiAlias(true);
    _metricValuePaint.setStrokeWidth(1);
    _metricValuePaint.setStyle(Paint.Style.FILL);
    _metricValuePaint.setColor(0xffffffff);
    _metricValuePaint.setTextAlign(Paint.Align.LEFT);
    _metricValuePaint.setTextSize(14f);
    _metricValuePaint.setFakeBoldText(true);

    _dayNamePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    _dayNamePaint.setAntiAlias(true);
    _dayNamePaint.setStrokeWidth(1);
    _dayNamePaint.setStyle(Paint.Style.FILL);
    _dayNamePaint.setColor(0x80ffffff);
    _dayNamePaint.setTextAlign(Paint.Align.RIGHT);
    _dayNamePaint.setTextSize(12f);
    _dayNamePaint.setFakeBoldText(true);

    _datePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    _datePaint.setAntiAlias(true);
    _datePaint.setStrokeWidth(1);
    _datePaint.setStyle(Paint.Style.FILL);
    _datePaint.setColor(0xffffffff);
    _datePaint.setTextAlign(Paint.Align.RIGHT);
    _datePaint.setTextSize(14f);
    _datePaint.setFakeBoldText(true);

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

    _timeSeriesPath = new Path();

    _metricArcBounds = new RectF(9, 9, 310, 310);

    _metricOKPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    _metricOKPaint.setAntiAlias(true);
    _metricOKPaint.setStrokeWidth(7);
    _metricOKPaint.setStrokeCap(Paint.Cap.BUTT);
    _metricOKPaint.setStyle(Paint.Style.STROKE);
    _metricOKPaint.setColor(0xff35b400);
    _metricOKPaint.setShadowLayer(4, 0, 0, 0xff35b400);

    _metricWarningPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    _metricWarningPaint.setAntiAlias(true);
    _metricWarningPaint.setStrokeWidth(7);
    _metricWarningPaint.setStrokeCap(Paint.Cap.BUTT);
    _metricWarningPaint.setStyle(Paint.Style.STROKE);
    _metricWarningPaint.setColor(0xfff4cd00);
    _metricWarningPaint.setShadowLayer(4, 0, 0, 0xfff4cd00);

    _metricErrorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    _metricErrorPaint.setAntiAlias(true);
    _metricErrorPaint.setStrokeWidth(7);
    _metricErrorPaint.setStrokeCap(Paint.Cap.BUTT);
    _metricErrorPaint.setStyle(Paint.Style.STROKE);
    _metricErrorPaint.setColor(0xffdb4437);
    _metricErrorPaint.setShadowLayer(4, 0, 0, 0xffdb4437);

    _background = createBackground(context);
    _backgroundPaint = new Paint();
    _backgroundPaint.setFilterBitmap(true);
  }

  private void createTimer() {
    final Handler timerHandler = new Handler() {
      @Override
      public void handleMessage(Message msg) {
        updateTime();
      }
    };

    TimerTask timerTask = new TimerTask() {
      @Override
      public void run() {
        timerHandler.sendEmptyMessage(0);
      }
    };

    Timer timer = new Timer();
    timer.scheduleAtFixedRate(timerTask, 0, 1000);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    float value = 0;
    if (!_values.isEmpty()) {
      value = _values.get(_values.size() - 1);
    }

    canvas.drawBitmap(_background, 0, 0, _backgroundPaint);

    // Day and date
    canvas.drawTextOnPath(_dayFormat.format(_calendar.getTime()).toUpperCase(),
                          _dateHeadingPath, 0, 0, _dayNamePaint);
    canvas.drawTextOnPath(_dateFormat.format(_calendar.getTime()),
                          _dateValuePath, 0, 0, _datePaint);

    // Metric value
    canvas.drawTextOnPath(_metricName.toUpperCase(), _metricHeadingPath, 0, 0, _metricNamePaint);
    canvas.drawTextOnPath((int)value + _metricUnit, _metricValuePath, 0, 0, _metricValuePaint);

    // Time-series
    _timeSeriesPath.reset();

    float x = 0;
    float y = 0;
    for (int i = 0; i < _values.size(); i++) {
      x = i * 1.7f;
      y = 60 - _values.get(i) * 60f / (_metricMaxValue * 2);
      if (y < 0f) {
        y = 0f;
      }

      if (i == 0) {
        _timeSeriesPath.moveTo(x, y);
      }
      else {
        _timeSeriesPath.lineTo(x, y);
      }
    }

    canvas.save(Canvas.MATRIX_SAVE_FLAG);
    canvas.translate(75, 200);
    canvas.drawPath(_timeSeriesPath, _timeSeriesPaint);
    canvas.drawCircle(x, y, 1.25f, _currentValuePaint);
    canvas.restore();

    // Metric arc
    Paint metricPaint;
    float metricAngle;

    if (value < _metricMaxValue * .9) {
      metricPaint = _metricOKPaint;
      metricAngle = 220 * (value / _metricMaxValue);
    }
    else if (value > _metricMaxValue) {
      metricPaint = _metricErrorPaint;
      metricAngle = 220 + 12.5f * value / _metricMaxValue;
    }
    else {
      metricPaint = _metricWarningPaint;
      metricAngle = 220 * (value / _metricMaxValue);
    }

    if (metricAngle > 270) {
      metricAngle = 270;
    }
    if (metricAngle < 10) {
      metricAngle = 10;
    }

    canvas.save(Canvas.MATRIX_SAVE_FLAG);
    canvas.rotate(135, 160, 160);
    canvas.drawArc(_metricArcBounds, 0, metricAngle, false, metricPaint);
    canvas.restore();

    // Hour hand
    canvas.save(Canvas.MATRIX_SAVE_FLAG);
    canvas.rotate(30 * (_time.hour + _time.minute / 60f) - 90, 160, 160);
    canvas.drawLine(165, 160, 225, 160, _hourHandPaint);
    canvas.restore();

    // Minute hand
    canvas.save(Canvas.MATRIX_SAVE_FLAG);
    canvas.rotate(6 * (_time.minute + _time.second / 60f) - 90, 160, 160);
    canvas.drawLine(165, 160, 270, 160, _minuteHandPaint);
    canvas.restore();

    // Second hand
    canvas.save(Canvas.MATRIX_SAVE_FLAG);
    canvas.rotate(6 * _time.second - 90, 160, 160);
    canvas.drawLine(165, 160, 280, 160, _secondHandPaint);
    canvas.restore();
  }

  public void updateMetrics(Map<String, Object> metricData) {
    _values = MetricSchema.getValues(metricData);
    _metricMaxValue = MetricSchema.getMaxValue(metricData);
    _metricUnit = MetricSchema.getUnitName(metricData);
    _metricName = MetricSchema.getDisplayName(metricData);

    invalidate();
  }

  public void updateTime() {
    _time.setToNow();
    invalidate();
  }
}
