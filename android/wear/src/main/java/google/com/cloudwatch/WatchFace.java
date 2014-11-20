// WatchFace.java
//

package google.com.cloudwatch;

import android.content.*;
import android.graphics.*;
import android.graphics.drawable.Drawable;
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
  private Paint _whiteFill;
  private Paint _textPaint;

  private Drawable _logo;

  private CircularArray<Float> _values;
  private Random _valueGenerator;

  public WatchFace(Context context) {
    super(context);

    _logo = context.getResources().getDrawable(R.drawable.cloud);

    _time = new Time(TimeZone.getDefault().getID());
    _message = "Cloud Watch";
    _values = new CircularArray<Float>(100);
    _valueGenerator = new Random();

    for (int i = 0; i < 50; i++) {
      _values.addLast(_valueGenerator.nextInt(40) - 20f);
    }

    _backgroundPaint = new Paint();
    _backgroundPaint.setColor(0xff202020);
    _backgroundPaint.setStyle(Paint.Style.FILL);

    _whitePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    _whitePaint.setAntiAlias(true);
    _whitePaint.setStrokeWidth(2);
    _whitePaint.setStrokeCap(Paint.Cap.BUTT);
    _whitePaint.setStyle(Paint.Style.STROKE);
    _whitePaint.setColor(0xffffffff);
    _whitePaint.setShadowLayer(4, 0, 0, 0xff0000ff);

    _whiteFill = new Paint(Paint.ANTI_ALIAS_FLAG);
    _whiteFill.setAntiAlias(true);
    _whiteFill.setStyle(Paint.Style.FILL);
    _whiteFill.setColor(0xffffffff);

    _textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    _textPaint.setAntiAlias(true);
    _textPaint.setStrokeWidth(1);
    _textPaint.setStyle(Paint.Style.FILL);
    _textPaint.setColor(0xffffffff);
    _textPaint.setTextAlign(Paint.Align.CENTER);
    _textPaint.setTextSize(10f);

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

    canvas.drawCircle(160, 160, 35, _whiteFill);
    _logo.setBounds(new Rect(130, 130, 190, 190));
    _logo.draw(canvas);

    canvas.save(Canvas.MATRIX_SAVE_FLAG);
    for (int i = 0; i < 12; i++) {
      canvas.drawLine(290, 160, 300, 160, _whitePaint);
      canvas.rotate(30f, 160f, 160f);
    }
    canvas.restore();
    canvas.drawCircle(160, 160, 140, _whitePaint);

    Path brandPath = new Path();
    brandPath.addArc(new RectF(40, 40, 280, 280), -180, 180);
    canvas.drawTextOnPath("Google Cloud Watch", brandPath, 0, 0, _textPaint);

    Path path = new Path();
    path.moveTo(75f, 240f);
    for (int i = 0; i < _values.size(); i++) {
      path.lineTo(i * 1.7f + 75, _values.get(i) + 240);
    }
    canvas.drawPath(path, _whitePaint);

    Path textPath = new Path();
    textPath.moveTo(75, 210);
    textPath.lineTo(245, 210);
    canvas.drawTextOnPath("Latency - 500ms", textPath, 0, 0, _textPaint);

    canvas.drawArc(new RectF(10, 10, 310, 310), 135, 270, false, _redPaint);

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
