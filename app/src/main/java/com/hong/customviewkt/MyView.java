package com.hong.customviewkt;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * @Description:
 * @Author: slh
 * @CreateDate: 2023/8/24 16:55
 * @UpdateUser: 更新者：
 * @UpdateDate: 2023/8/24 16:55
 * @UpdateRemark: 更新说明：
 * @Version: 2.8.0
 */
public class MyView extends View {

    public MyView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        // 给该view设置点击事件，每点击一次，都会执行一次scrollTo方法
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                v.scrollBy(0, 20);
            }
        });

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 将这个view的大小固定为 500x500
        setMeasuredDimension(500, 500);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 将该view的背景设置为红色
        canvas.drawColor(Color.RED);

        // 在view中间绘制一个半径为50的圆，默认paint，所以这个圆是个黑色的
        canvas.drawCircle(250, 250, 50, new Paint());

    }
}
