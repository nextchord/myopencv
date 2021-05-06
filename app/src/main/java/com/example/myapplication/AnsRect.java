package com.example.myapplication;

import org.opencv.core.Point;
import org.opencv.core.Rect;

/**
 * 矩形轮廓，实现了comparable接口，用于答题区域轮廓排序
 */
public class AnsRect extends Rect implements Comparable<AnsRect> {

    public AnsRect(Point p1, Point p2){
        super(p1, p2);
    }

    public static int getDeviationY() {
        return deviationY;
    }

    private static int deviationY = 15;  // 排序时y值允许的偏差，在这个值以内都认为是同一行

    //  根据y值排序，y值deviationY以内认为同一行，根据x排序
    @Override
    public int compareTo(AnsRect o) {

        double absY = Math.abs(this.y - o.y);
        // 误差在deviationY以内都认为是同一行
        if(absY>deviationY){
            if (this.y - o.y > 0) {
                return 1;
            } else if (this.y - o.y < 0) {
                return -1;
            }
        }

        if(this.x - o.x >0){
            return 1;
        }else if(this.x - o.x <0){
            return -1;
        }
        return 0;
    }
}
