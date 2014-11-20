// WatchFace.java
//

package google.com.cloudwatch;

import android.content.*;
import android.graphics.*;
import android.os.*;
import android.support.v4.util.*;
import android.text.format.*;
import android.view.*;

import java.util.*;

public class WatchFace extends View {

  private Time _time;
  private String _message;

  private Paint _backgroundPaint;
  private Paint _greenPaint;
  private Paint _redPaint;
  private Paint _blackPaint;
  private Paint _whitePaint;

  private CircularArray<Float> _values;
  private Random _valueGenerator;

  public WatchFace(Context context) {
    super(context);

    _time = new Time(TimeZone.getDefault().getID());
    _message = "Cloud Watch";
    _values = new CircularArray<Float>(100);
    _valueGenerator = new Random();

    _backgroundPaint = new Paint();
    _backgroundPaint.setColor(0xff202020);
    _backgroundPaint.setStyle(Paint.Style.FILL);

    _whitePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    _whitePaint.setAntiAlias(true);
    _whitePaint.setStrokeWidth(2);
    _whitePaint.setStrokeCap(Paint.Cap.BUTT);
    _whitePaint.setStyle(Paint.Style.STROKE);
    _whitePaint.setColor(0xffffffff);

    _blackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    _blackPaint.setAntiAlias(true);
    _blackPaint.setStrokeWidth(10);
    _blackPaint.setStrokeCap(Paint.Cap.BUTT);
    _blackPaint.setStyle(Paint.Style.STROKE);
    _blackPaint.setColor(0xff000000);

    _redPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    _redPaint.setAntiAlias(true);
    _redPaint.setStrokeWidth(10);
    _redPaint.setStrokeCap(Paint.Cap.BUTT);
    _redPaint.setStyle(Paint.Style.STROKE);
    _redPaint.setColor(0xff800000);
    _redPaint.setShadowLayer(10, 0, 0, 0xff0000ff);

    _greenPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    _greenPaint.setAntiAlias(true);
    _greenPaint.setStrokeWidth(4);
    _greenPaint.setStrokeCap(Paint.Cap.BUTT);
    _greenPaint.setStyle(Paint.Style.STROKE);
    _greenPaint.setColor(0xff008000);
    _greenPaint.setShadowLayer(4, 0, 0, 0xff0000ff);

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

    canvas.drawRect(0, 0, 320, 320, _backgroundPaint);
    canvas.drawText(_message, 30, 160, _whitePaint);

    // canvas.drawLine(0, 0, 320, 320, _paint);
    canvas.drawArc(new RectF(10, 10, 310, 310), 135, 270, false, _redPaint);
    // canvas.drawArc(new RectF(30, 30, 290, 290), 315, 90, false, _blackPaint);

    canvas.drawCircle(160, 160, 140, _whitePaint);

    canvas.save(Canvas.MATRIX_SAVE_FLAG);
    for (int i = 0; i < 12; i++) {
      canvas.drawLine(290, 160, 300, 160, _whitePaint);
      canvas.rotate(30f, 160f, 160f);
    }
    canvas.restore();

    canvas.save(Canvas.MATRIX_SAVE_FLAG);
    canvas.rotate(30 * (_time.hour + _time.minute / 60f) - 90, 160, 160);
    canvas.drawLine(160, 160, 210, 160, _greenPaint);
    canvas.restore();

    canvas.save(Canvas.MATRIX_SAVE_FLAG);
    canvas.rotate(6 * (_time.minute + _time.second / 60f) - 90, 160, 160);
    canvas.drawLine(160, 160, 250, 160, _greenPaint);
    canvas.restore();

    canvas.save(Canvas.MATRIX_SAVE_FLAG);
    canvas.rotate(6 * _time.second - 90, 160, 160);
    canvas.drawLine(160, 160, 250, 160, _whitePaint);
    canvas.restore();


    Path path = new Path();
    path.moveTo(75f, 220f);

    for (int i = 0; i < _values.size(); i++) {
      path.lineTo(i * 1.7f + 75, _values.get(i) + 220);
    }

    canvas.drawPath(path, _whitePaint);
  }

  public void updateMessage(String message) {
    _message = message;
    invalidate();
  }

  public void updateTime() {
    _time.setToNow();

    int hour = _time.hour;
    int minute = _time.minute;
    int second = _time.second;

    if (_values.size() == 100) {
      _values.popFirst();
    }
    _values.addLast(_valueGenerator.nextInt(40) - 20f);

    invalidate();
  }
}
