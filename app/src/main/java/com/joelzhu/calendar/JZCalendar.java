package com.joelzhu.calendar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 作者：JoelZhu
 * 时间：2016年12月02日 10:39
 * 作用：自定义日历控件
 */
public class JZCalendar extends View {
    // 画笔对象
    private Paint paint;
    // Rect
    private Rect rect;
    // Path
    private Path path;

    // 控件宽度的最小单位
    private float minUnitWidth;
    // 控件高度的最小单位
    private float minUnitHeight;

    // 月份条背景
    private int monthBarBackground;
    // 月份条字体颜色
    private int monthBarTextColor;
    // 月份条字体大小
    private float monthBarTextSize;
    // 日历区域背景
    private int dateBackground;
    // 周始周末字体颜色
    private int weekendTextColor;
    // 日期字体颜色
    private int dateTextColor;
    // 日期字体大小
    private float dateTextSize;
    // 其他月份字体颜色
    private int otherTextColor;
    // 今日字体颜色
    private int todayTextColor;
    // 选中日期字体颜色
    private int selectTextColor;

    // 本月日期数组
    private List<Integer> date;
    // "今日"在数组中的位置
    private int todayPosition;
    // 本月是今年的第几个月
    private int monthOfYear;
    // 今年
    private int thisYear;
    // 现在显示的月份偏差
    private int showMonthOffset;
    // 当前显示的日期坐标
    private int selectedPosition;
    // 当前显示的日期的月份偏移量
    private int selectedMonthOffset;
    // 星期条
    private final String[] weeks = {"日", "一", "二", "三", "四", "五", "六"};

    // 日期点击事件监听器
    private OnDateClickListener onDateClickListener;
    // 日期长按事件监听器
    private OnDateLongClickListener onDateLongClickListener;
    // 上一次Down触发的时点
    private long lastDownAt;
    // 上一次Down的X坐标
    private float lastDownX;
    // 上一次Down的Y坐标
    private float lastDownY;

    /**
     * 构造函数
     *
     * @param context Context
     */
    public JZCalendar(Context context) {
        super(context);

        // 构建控件
        initCalendar(context, null);
    }

    /**
     * 构造函数
     *
     * @param context Context
     * @param attrs   属性对象
     */
    public JZCalendar(Context context, AttributeSet attrs) {
        super(context, attrs);

        // 构建控件
        initCalendar(context, attrs);
    }

    /**
     * 构造函数
     *
     * @param context      Context
     * @param attrs        属性对象
     * @param defStyleAttr 默认的Style
     */
    public JZCalendar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // 构建控件
        initCalendar(context, attrs);
    }

    /**
     * OnMeasure
     *
     * @param widthMeasureSpec  父布局提供的水平的空间要求
     * @param heightMeasureSpec 父布局提供的垂直的空间要求
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 获取高度和宽度的指定模式
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        // 控件的高宽
        int width;
        int height;

        // 判断指定控件宽度的模式
        if (widthMode == MeasureSpec.EXACTLY) {
            // 以精确值作为宽
            width = widthSize;
            // 计算控件宽度的最小单位
            minUnitWidth = (float) width / 7;
        } else {
            // 计算控件宽度的最小单位
            minUnitWidth = dp2Px(48);
            // 以每个日期48dp作为最小单位
            width = dp2Px(336);
        }

        // 判断指定控件高度的模式
        if (heightMode == MeasureSpec.EXACTLY) {
            // 以精确值作为高
            height = heightSize;
            // 计算控件高度的最小单位
            minUnitHeight = (float) height / 9;
        } else {
            // 计算控件高度的最小单位(宽的四分之三)
            minUnitHeight = dp2Px(36);
            // 以每个日期48dp作为最小单位
            height = dp2Px(324);
        }

        // 构建高宽指定的控件
        setMeasuredDimension(width, height);
    }

    /**
     * OnDraw
     *
     * @param canvas Canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        // 绘制月份条
        drawMonthBar(canvas);

        // 绘制星期条
        drawWeekBar(canvas);

        // 绘制日期
        drawDateItem(canvas);
    }

    /**
     * OnTouch
     *
     * @param event Touch事件
     * @return 是否消费这个事件
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            // 按下事件
            case MotionEvent.ACTION_DOWN:
                // 记录这次Down触发的时点
                lastDownAt = new Date().getTime();
                // 记录这次Down的X和Y的坐标
                lastDownX = event.getX();
                lastDownY = event.getY();
                return true;

            // 弹起事件
            case MotionEvent.ACTION_UP:
                // 判断上一次点击时点是否为0，如果为0，说明点击事件已经被移动取消了
                if (lastDownAt != 0) {
                    // 比较Up的时点和Down相差是否超过0.5秒
                    if (new Date().getTime() - lastDownAt <= 500) {
                        // 小于1秒，判定为点击事件，触发点击事件处理
                        calculateClickPosition(lastDownX, lastDownY);
                    } else {
                        // 大于1秒，判定为长按事件，触发长按事件处理
                        calculateLongClickPosition(lastDownX, lastDownY);
                    }
                }
                return true;

            // 移动事件
            case MotionEvent.ACTION_MOVE:
                // 判断上一次点击时点是否为0，如果为0，说明点击事件已经被取消了
                if (lastDownAt != 0) {
                    // 获取这次Up的X和Y的坐标
                    float nowUpX = event.getX();
                    float nowUpY = event.getY();
                    // 判断移动区域是否超过最小单元单位的一半
                    if (Math.abs(lastDownX - nowUpX) >= minUnitWidth / 2 ||
                            Math.abs(lastDownY - nowUpY) >= minUnitHeight / 2) {
                        // 如果超过最小单元单位的一半，判定为移动
                        lastDownAt = 0;
                    }
                }
                return true;
        }

        return super.onTouchEvent(event);
    }

    /**
     * 构建控件
     */
    private void initCalendar(Context context, AttributeSet attrs) {
        // 执行绘图函数
        setWillNotDraw(false);

        // 初始化之前的处理
        doBeforeInit(context, attrs);

        // 初始化数据
        getCalendarMonth(0);
    }

    /**
     * 绘制月份条
     *
     * @param canvas Canvas对象
     */
    private void drawMonthBar(Canvas canvas) {
        // 设置月份条背景样式
        paint.reset();
        paint.setAntiAlias(true);
        paint.setColor(monthBarBackground);
        // 绘制月份条背景
        canvas.drawRect(0, 0, getMeasuredWidth(), minUnitHeight * 1.5f, paint);

        // 设置月份条文字样式
        paint.reset();
        paint.setAntiAlias(true);
        paint.setColor(monthBarTextColor);
        paint.setTextSize(monthBarTextSize);
        // 月份条文字
        String formatString = "%d年%02d月";
        String monthText = String.format(Locale.getDefault(), formatString, thisYear, monthOfYear + 1);
        Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
        final float heightPosition = (minUnitHeight * 1.5f - fontMetrics.bottom + fontMetrics.top) / 2 -
                fontMetrics.top;
        paint.getTextBounds(monthText, 0, monthText.length(), rect);
        // 绘制月份条显示内容
        canvas.drawText(monthText, getWidth() / 2 - rect.width() / 2, heightPosition, paint);

        // 设置月份条月份跳转按钮样式
        paint.reset();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(monthBarTextColor);
        paint.setStrokeWidth(dp2Px(3));
        // 计算月份条按钮八等分单位(由于是1.5倍最小单位，(*3/2)/8)
        float minUnit = minUnitHeight * 3 / 16;
        // 计算右箭头X轴偏移量
        float offsetX = getMeasuredWidth() - minUnitHeight * 1.5f;
        // 左箭头
        path.reset();
        path.moveTo(minUnit * 5, minUnit * 3);
        path.lineTo(minUnit * 4, minUnit * 4);
        path.lineTo(minUnit * 5, minUnit * 5);
        canvas.drawPath(path, paint);
        // 右箭头
        path.reset();
        path.moveTo(offsetX + minUnit * 4, minUnit * 3);
        path.lineTo(offsetX + minUnit * 5, minUnit * 4);
        path.lineTo(offsetX + minUnit * 4, minUnit * 5);
        canvas.drawPath(path, paint);
    }

    /**
     * 绘制星期条
     *
     * @param canvas Canvas对象
     */
    private void drawWeekBar(Canvas canvas) {
        // 绘制日期背景
        paint.reset();
        paint.setAntiAlias(true);
        paint.setColor(dateBackground);
        canvas.drawRect(0, minUnitHeight * 1.5f, getMeasuredWidth(), minUnitHeight * 3, paint);

        for (int i = 0; i < 7; i++) {
            // 绘制星期条文字
            paint.reset();
            paint.setAntiAlias(true);
            paint.setTextSize(dateTextSize);
            // 设置文字颜色
            paint.setColor(i == 0 || i == 6 ? weekendTextColor : dateTextColor);
            paint.getTextBounds(weeks[i], 0, 1, rect);
            Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
            final float width = (minUnitWidth - rect.width()) / 2 + (minUnitWidth * i);
            final float height = (minUnitHeight * 1.5f - fontMetrics.bottom + fontMetrics.top) / 2 -
                    fontMetrics.top;
            canvas.drawText(weeks[i], width, height + minUnitHeight * 1.5f, paint);
        }
    }

    /**
     * 绘制日期
     *
     * @param canvas Canvas对象
     */
    private void drawDateItem(Canvas canvas) {
        // 绘制日期背景
        paint.reset();
        paint.setAntiAlias(true);
        paint.setColor(dateBackground);
        canvas.drawRect(0, minUnitHeight * 3, getMeasuredWidth(), getMeasuredHeight(), paint);

        for (int position = 0; position < 42; position++) {
            float leftPosition = (position % 7) * minUnitWidth;
            float topPosition = minUnitHeight * 3 + minUnitHeight * (position / 7);

            // 判断当前日期是否是选中日期(默认今日)
            if ((selectedPosition != 0 && position == selectedPosition && selectedMonthOffset == showMonthOffset) ||
                    (showMonthOffset == 0 && position == todayPosition && selectedPosition == 0)) {
                // 绘制当前选中日期的背景
                paint.reset();
                paint.setAntiAlias(true);
                paint.setColor(todayTextColor);
                float radius = minUnitWidth <= minUnitHeight ?
                        minUnitWidth * 2 / 5 : minUnitHeight * 2 / 5;
                canvas.drawCircle(leftPosition + minUnitWidth / 2, topPosition + minUnitHeight / 2,
                        radius, paint);

                // 绘制日期文字
                paint.reset();
                paint.setAntiAlias(true);
                paint.setTextSize(dateTextSize);
                paint.setColor(selectTextColor);

                String dateString = date.get(position) + "";
                paint.getTextBounds(dateString, 0, dateString.length(), rect);
                Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
                final float width = (minUnitWidth - rect.width()) / 2 + (minUnitWidth *
                        ((position % 7)));
                final float height = (minUnitHeight - fontMetrics.bottom + fontMetrics.top) / 2 -
                        fontMetrics.top + minUnitHeight * ((position / 7) + 3);
                canvas.drawText(dateString, width, height, paint);
            } else {
                // 绘制日期文字
                paint.reset();
                paint.setAntiAlias(true);
                paint.setTextSize(dateTextSize);
                // 设置今日的字体颜色
                if (position == todayPosition && showMonthOffset == 0)
                    paint.setColor(todayTextColor);
                // 设置其他月份的字体颜色
                else if (isPreviousMonth(position) || isNextMonth(position))
                    paint.setColor(otherTextColor);
                // 设置周末的字体颜色
                else if (position % 7 == 0 || position % 7 == 6)
                    paint.setColor(weekendTextColor);
                // 设置正常日期的字体颜色
                else
                    paint.setColor(dateTextColor);

                String dateString = date.get(position) + "";
                paint.getTextBounds(dateString, 0, dateString.length(), rect);
                Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
                final float width = (minUnitWidth - rect.width()) / 2 + (minUnitWidth *
                        ((position % 7)));
                final float height = (minUnitHeight - fontMetrics.bottom + fontMetrics.top) / 2 -
                        fontMetrics.top + minUnitHeight * ((position / 7) + 3);
                canvas.drawText(dateString, width, height, paint);
            }
        }
    }

    /**
     * 初始化之前的处理
     */
    private void doBeforeInit(Context context, AttributeSet attrs) {
        // 初始化数组
        date = new ArrayList<>();
        // 初始化月份偏差
        showMonthOffset = 0;

        // 初始化画笔
        paint = new Paint();
        // 初始化Rect
        rect = new Rect();
        // 初始化Path
        path = new Path();

        // 读取控件属性
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.JZCalendar);
        // 判断控件属性是否为空
        if (typedArray != null) {
            // 月份条背景
            monthBarBackground = typedArray.getColor(R.styleable.JZCalendar_monthBarBackground, Color.BLACK);
            // 月份条字体颜色
            monthBarTextColor = typedArray.getColor(R.styleable.JZCalendar_monthBarTextColor, Color.WHITE);
            // 月份条字体大小
            monthBarTextSize = typedArray.getDimensionPixelSize(R.styleable.JZCalendar_monthBarTextSize, dp2Px(14));
            // 日历区域背景
            dateBackground = typedArray.getColor(R.styleable.JZCalendar_dateBackground, monthBarTextColor);
            // 日期字体颜色
            dateTextColor = typedArray.getColor(R.styleable.JZCalendar_dateTextColor, monthBarBackground);
            // 周始周末字体颜色
            weekendTextColor = typedArray.getColor(R.styleable.JZCalendar_weekendTextColor, dateTextColor);
            // 日期字体大小
            dateTextSize = typedArray.getDimensionPixelSize(R.styleable.JZCalendar_dateTextSize, dp2Px(14));
            // 其他月份字体颜色
            otherTextColor = typedArray.getColor(R.styleable.JZCalendar_otherTextColor, dateTextColor);
            // 今日字体颜色
            todayTextColor = typedArray.getColor(R.styleable.JZCalendar_todayTextColor, dateTextColor);
            // 选中日期字体颜色
            selectTextColor = typedArray.getColor(R.styleable.JZCalendar_selectTextColor, dateTextColor);
            // 回收属性数组
            typedArray.recycle();
        }
    }

    /**
     * 获取本月需要显示的日期
     *
     * @param dateSelected 点击事件选择的日期
     */
    private void getCalendarMonth(int dateSelected) {
        // 初始化数组
        date.clear();
        // 获取日历类实例
        Calendar calendar = Calendar.getInstance();
        // 设置今日在本月中是第几天
        todayPosition = calendar.get(Calendar.DAY_OF_MONTH) - 1;
        // 设置日历类到本月的1号
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + showMonthOffset, 1);
        // 得到本月1号是周几
        int dateOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        // 得到本月是第几个月
        monthOfYear = calendar.get(Calendar.MONTH);
        // 得到今年
        thisYear = calendar.get(Calendar.YEAR);

        // 判断本月1号是否是周日
        if (dateOfWeek == 1) {
            // 如果本月1号是周日，添加上上个月的月末
            dateOfWeek = 8;
        }

        // 变化为显示的第一周周日的日期
        calendar.add(Calendar.DATE, -dateOfWeek);
        // 添加上个月需要显示的日期
        addLastMonth(calendar.get(Calendar.DATE), dateOfWeek);

        // 重置日历类
        calendar.add(Calendar.DATE, dateOfWeek);
        // 遍历当前月份需要显示的日期
        int maxDate = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 1; i <= maxDate; i++) {
            // 判断当前选择日是否为空
            if (i == dateSelected) {
                // 计算出选择的日期在数组中的位置
                selectedPosition = (i + dateOfWeek - 1) - 1;
            }
            date.add(i);
        }

        // 添加下个月需要显示的日期(补充满42天)
        for (int nextMonth = 1; date.size() < 42; nextMonth++) {
            date.add(nextMonth);
        }
    }

    /**
     * 在日历中添加上个月需要显示的日期
     *
     * @param sundayDate 周日日期
     * @param dateOfWeek 1号是本周的第几天
     */
    private void addLastMonth(int sundayDate, int dateOfWeek) {
        // 从周日日期遍历到1号
        for (int i = 1; i < dateOfWeek; i++) {
            date.add(sundayDate + i);
            // 修正今日在数组中的位置
            todayPosition++;
        }
    }

    /**
     * 判断数组中的某一天是否属于上一个月
     *
     * @param position 数组中的某一天
     * @return 该天是否属于上一个月
     */
    private boolean isPreviousMonth(int position) {
        return position <= 13 && date.get(position) > 13;
    }

    /**
     * 判断数组中的某一天是否属于下一个月
     *
     * @param position 数组中的某一天
     * @return 该天是否属于下一个月
     */
    private boolean isNextMonth(int position) {
        return position > 28 && date.get(position) <= 21;
    }

    /**
     * 点击事件判定
     *
     * @param clickX 点击的X坐标
     * @param clickY 点击的Y坐标
     */
    private void calculateClickPosition(float clickX, float clickY) {
        // 点击的是前一个月按钮
        if (clickX > 0 && clickX < minUnitHeight * 1.5f && clickY > 0 && clickY < minUnitHeight * 1.5f) {
            // 修改月份偏移量
            showMonthOffset--;
            // 获取前一个月的日历
            getCalendarMonth(0);
            // 刷新控件
            invalidate();
        }
        // 点击的是后一个月按钮
        else if (clickX > getMeasuredWidth() - minUnitHeight * 1.5f && clickX < getMeasuredWidth() &&
                clickY > 0 && clickY < minUnitHeight * 1.5f) {
            // 修改偏移量
            showMonthOffset++;
            // 获取后一个月的日历
            getCalendarMonth(0);
            // 刷新控件
            invalidate();
        }
        // 点击的是日期区域
        else if (clickY > minUnitHeight * 3 && clickY < getMeasuredHeight()) {
            // 计算出X和Y的整数位置
            int xPosition = (int) (clickX / minUnitWidth);
            int yPosition = (int) ((clickY - minUnitHeight * 3) / minUnitHeight);
            // 计算出点击的区域在数组中的位置
            selectedPosition = yPosition * 7 + xPosition;
            selectedMonthOffset = 0;

            // 计算是否含有月份点击偏移量
            int monthOffset = 0;
            if (isPreviousMonth(selectedPosition)) {
                // 点击了上个月显示的日期
                monthOffset = -1;
            } else if (isNextMonth(selectedPosition)) {
                // 点击了下个月显示的日期
                monthOffset = 1;
            }

            // 获取月份
            int monthInt = monthOfYear + monthOffset + 1;
            // 判断点击的月份是否为12月(去年的12月)
            if (monthInt == 0) {
                // 修正年份偏移量
                thisYear--;
                monthInt = 12;
            }
            // 判断点击的月份是否为1月(明年的1月)
            else if (monthInt == 13) {
                // 修正年份偏移量
                thisYear++;
                monthInt = 1;
            }

            // 响应点击事件
            if (onDateClickListener != null) {
                onDateClickListener.OnDateClick(thisYear, monthInt, date.get(selectedPosition));
            }

            // 如果点击的日期不为当前显示月的日期，修正显示月份
            showMonthOffset = showMonthOffset + monthOffset;
            selectedMonthOffset = showMonthOffset;
            // 判断月份偏移量是否为空
            if (monthOffset != 0) {
                // 重新计算日历，并且传入点击的日期
                getCalendarMonth(date.get(selectedPosition));
            } else {
                // 重新计算日历
                getCalendarMonth(0);
            }
            invalidate();
        }
    }

    /**
     * 长按事件判定
     *
     * @param clickX 点击的X坐标
     * @param clickY 点击的Y坐标
     */
    private void calculateLongClickPosition(float clickX, float clickY) {
        // 判断长按事件是否为空
        if (onDateLongClickListener == null) {
            // 作为点击事件处理
            calculateClickPosition(clickX, clickY);
        }
        // 长按事件不为空，继续处理
        else {
            // 点击的是前一个月按钮
            if (clickX > 0 && clickX < minUnitHeight * 1.5f && clickY > 0 && clickY < minUnitHeight * 1.5f) {
                // 修改月份偏移量
                showMonthOffset--;
                // 获取前一个月的日历
                getCalendarMonth(0);
                // 刷新控件
                invalidate();
            }
            // 点击的是后一个月按钮
            else if (clickX > getMeasuredWidth() - minUnitHeight * 1.5f && clickX < getMeasuredWidth() &&
                    clickY > 0 && clickY < minUnitHeight * 1.5f) {
                // 修改偏移量
                showMonthOffset++;
                // 获取后一个月的日历
                getCalendarMonth(0);
                // 刷新控件
                invalidate();
            }
            // 点击的是日期区域
            else if (clickY > minUnitHeight * 3 && clickY < getMeasuredHeight()) {
                // 计算出X和Y的整数位置
                int xPosition = (int) (clickX / minUnitWidth);
                int yPosition = (int) ((clickY - minUnitHeight * 3) / minUnitHeight);
                // 计算出点击的区域在数组中的位置
                selectedPosition = yPosition * 7 + xPosition;
                selectedMonthOffset = 0;

                // 计算是否含有月份点击偏移量
                int monthOffset = 0;
                if (isPreviousMonth(selectedPosition)) {
                    // 点击了上个月显示的日期
                    monthOffset = -1;
                } else if (isNextMonth(selectedPosition)) {
                    // 点击了下个月显示的日期
                    monthOffset = 1;
                }

                // 获取月份
                int monthInt = monthOfYear + monthOffset + 1;
                // 判断点击的月份是否为12月(去年的12月)
                if (monthInt == 0) {
                    // 修正年份偏移量
                    thisYear--;
                    monthInt = 12;
                }
                // 判断点击的月份是否为1月(明年的1月)
                else if (monthInt == 13) {
                    // 修正年份偏移量
                    thisYear++;
                    monthInt = 1;
                }

                // 响应点击事件
                if (onDateLongClickListener != null) {
                    onDateLongClickListener.OnDateLongClick(thisYear, monthInt, date.get(selectedPosition));
                }
            }
        }
    }

    /**
     * 日期点击Interface
     */
    public interface OnDateClickListener {
        /**
         * 日期点击事件
         *
         * @param clickYear  点击的年
         * @param clickMonth 点击的月
         * @param clickDate  点击的日
         */
        void OnDateClick(int clickYear, int clickMonth, int clickDate);
    }

    /**
     * 设置日期点击监听事件
     *
     * @param listener 日期点击监听事件
     */
    public void setOnDateClickListener(OnDateClickListener listener) {
        this.onDateClickListener = listener;
    }

    /**
     * 日期长按Interface
     */
    public interface OnDateLongClickListener {
        /**
         * 日期长按事件
         *
         * @param clickYear  长按的年
         * @param clickMonth 长按的月
         * @param clickDate  长按的日
         */
        void OnDateLongClick(int clickYear, int clickMonth, int clickDate);
    }

    /**
     * 设置日期长按监听事件
     *
     * @param listener 日期长按监听事件
     */
    public void setOnDateLongClickListener(OnDateLongClickListener listener) {
        this.onDateLongClickListener = listener;
    }

    /**
     * 将DP单位的值转成为PX单位的值
     *
     * @param dpValue 换算前的DP值
     * @return 换算后的PX值
     */
    private int dp2Px(float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}