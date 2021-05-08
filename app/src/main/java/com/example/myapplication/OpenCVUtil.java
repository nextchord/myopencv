package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.opencv.imgproc.Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C;
import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.COLOR_GRAY2BGR;
import static org.opencv.imgproc.Imgproc.RETR_LIST;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY_INV;

public class OpenCVUtil {
    //临时图片保存地址
    //private static String extStorageDirectory;

    private final double ANSWER_THRESHOLD = 0.35; // 判断一个选项是否被选中的阈值，白色像素占方框的比重

    private static final String GET_ANS_AREA_ERR = "获取答题卡区域失败，请保留答题卡边框";


//    public String getExtStorageDirectory(){
//        return extStorageDirectory;
//    }

    public HashMap<Integer, Integer> getAns(String path, Context context){
//        extStorageDirectory = context.getExternalFilesDir("/temp").getAbsolutePath();


        FileInputStream fs = null;
        try {
            fs = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Bitmap bitmap  = BitmapFactory.decodeStream(fs);

        // 读取图片
        Mat src = new Mat();
        Utils.bitmapToMat(bitmap,src);


        // 保存原始图片
//        Imgcodecs.imwrite(extStorageDirectory + File.separator + "src.jpg", src);

        // 转为灰度图像
        Mat gray = new Mat();
        if(src.channels() != 1)
            Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        else
            gray = src.clone();
//        Imgcodecs.imwrite(extStorageDirectory + File.separator + "gray.jpg", gray);
        // 进行二值化、边缘检测等处理
        gray = initImg(gray);

        Mat ansArea;
        //获取答题区域图片
        ansArea = getAnsArea(src, gray);

        if (ansArea == null) {
            Toast.makeText(context, GET_ANS_AREA_ERR, Toast.LENGTH_SHORT).show();
            return null;
        }

        Mat img = new Mat();
        ansArea.copyTo(img);

        Imgproc.cvtColor(img, img, COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(img, img, new Size(9, 9), 0);
        Mat process = new Mat();
        img.copyTo(process);


        // 通过二值化和膨胀获得填涂的答案
        Imgproc.adaptiveThreshold(process, process, 255, ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY_INV, 61, 10);
        // 因为和下一步获取答题区域预处理相同，这里复制process给下一步使用避免重复处理
        Mat area = new Mat();
        process.copyTo(area);
        // 腐蚀获取填涂的选项
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));
        //Imgproc.erode(process, process, element);
        Imgproc.threshold(process, process, 64, 255, Imgproc.THRESH_BINARY);
//        Imgcodecs.imwrite(extStorageDirectory + File.separator + "select.jpg", process);


        // 获取答题整体区域
        element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 1));
        Imgproc.dilate(area, area, element);
        Imgproc.dilate(area, area, element);
        Imgproc.dilate(area, area, element);
        Imgproc.dilate(area, area, element);
        Imgproc.dilate(area, area, element);
        Imgproc.dilate(area, area, element);
        Imgproc.dilate(area, area, element);
        Imgproc.dilate(area, area, element);
        element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, 5));
        Imgproc.erode(area, area, element);
//        Imgcodecs.imwrite(extStorageDirectory + File.separator + "ansArea.jpg", area);

        List<AnsRect> rects = new ArrayList<AnsRect>();

        // 用于展示处理结果
        Mat show = new Mat();
        Imgproc.cvtColor(process, show, COLOR_GRAY2BGR);

        // 寻找轮廓，保存到列表中
        Mat hierarchy = new Mat();
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(area, contours, hierarchy, RETR_LIST, CHAIN_APPROX_SIMPLE);
        for (int i = 0; i < contours.size(); i++) {
            Rect rect = Imgproc.boundingRect(contours.get(i));
            // 过滤掉不是选项的轮廓
            if (rect.width > 550 || rect.height > 100 || rect.width < 150 || rect.height < 10) {
                continue;
            }
            // 将找到的矩形轮廓保存到列表，后面进行筛选
            Point p1 = new Point(rect.x, rect.y);
            Point p2 = new Point(rect.x + rect.width, rect.y + rect.height);
            AnsRect temp = new AnsRect(p1, p2);
            rects.add(temp);
            Imgproc.rectangle(show, p1, p2, new Scalar(255, 0, 255, 1), 1);
        }
//        Imgcodecs.imwrite(extStorageDirectory + File.separator + "rect.jpg", show);

        // 把获得的矩形根据题目号进行排序，获取每一道题的答案区域
        Collections.sort(rects);

        // 识别选项
        HashMap<Integer, Integer> answers = getAnswers(rects, process, ansArea);
//        if (answers.isEmpty()) {
//            Toast.makeText(context, "没有识别到题目,请重新选择图片", Toast.LENGTH_SHORT).show();
//        }

        //释放不需要的资源
        src.release();
        gray.release();
        ansArea.release();
        process.release();
        hierarchy.release();
        show.release();
        img.release();


        // 返回识别出的列表
        return answers;

    }


    /**
     * 传入识别出的所有题目的轮廓，返回每一题识别的结果
     * @param rects
     * @param ans
     * @param src : 作图展示结果
     * @return
     */
    private HashMap<Integer, Integer> getAnswers(List<AnsRect> rects, Mat ans, Mat src) {

        // 保存结果
        HashMap<Integer, Integer> answers = new HashMap<>();

        // 每一次横向遍历题目，左右间隔5题， 每w五行后下一次leftNum为上一次rightNum+1，否则为leftNum+1
        // 两题间y绝对值相差deviationY以内认为是同一行
        int leftNum = 1;   // 每一行最左边题号
        int rightNum = 1;     // 每一行最右边题号
        AnsRect leftRect = null;   // 上一题的区域
        AnsRect rightRect = null;   // 当前题目区域

        // 判断找不到或者只有一题的情况
        if (rects.size() < 1) {
            return answers;
        }

        // 先计算第一题
        answers.put(1, judge(rects.get(0), ans, src));
        // 用于计算是否遍历了五行
        int count = 1;
        // 从第二个轮廓开始
        for (int i = 0; i < rects.size()-1; i++) {
            leftRect = rects.get(i);
            rightRect = rects.get(i+1);
            // 判断是否同一行
            if (Math.abs(rightRect.y - leftRect.y) < AnsRect.getDeviationY() ) {
                //若在同一行后一题题号为前一题+3
                rightNum = rightNum + 3;
            } else {
                if(count<3){
                    // 换行，但是不够五行的情况
                    leftNum = leftNum+1;
                    rightNum = leftNum;
                    count ++;
                } else {
                    count = 1;
                    leftNum = rightNum + 1;
                    rightNum = leftNum;
                }
            }
            int answer = judge(rightRect, ans, src);
            answers.put(rightNum, answer);
        }
//        Imgcodecs.imwrite(extStorageDirectory + File.separator + "result.jpg", src);
        return answers;
    }

    /**
     * 给定题目区域，找到填涂的答案；-1表示多选，0表示没找到答案，1表示a，2表示b，依次类推
     * 白色占比超过阈值即认为填涂
     *
     * @param rect：某一题的区域
     * @param ans：图像
     * @return
     */
    private int judge(AnsRect rect, Mat ans, Mat src) {

        // 保存每一题的部分
        Mat res = new Mat();
        // 将该区域划分四个区域，分别对应四个选项，根据白色像素占的比例判断是否填写答案
        rect.width = rect.width / 4;
        boolean selected = false;
        int answer = 0;
        for (int j = 0; j < 4; j++) {
            if(j>0){
                rect.x = rect.x + rect.width ;
            }
            res = ans.submat(rect);
            byte[] data = new byte[res.rows() * res.cols()];
            res.get(0, 0, data);
            int count = 0;
            for (int k = 0; k < data.length; k++) {
                if (data[k] != 0) {
                    count += 1;
                }
            }
            // 白色像素大于阈值认为填涂,判断是否存在多选的情况
            if ((double) count / data.length > ANSWER_THRESHOLD) {
//                Log.d("image", "白色像素： "+count+" 总像素："+data.length);
                // 标记出识别的选项
                Point p1 = new Point(rect.x, rect.y);
                Point p2 = new Point(rect.x+rect.width, rect.y+rect.height);
                Imgproc.rectangle(src, p1, p2, new Scalar(0, 0, 255, 1), 1);
                if (selected) {
                    answer = -1;
                    break;
                }
                answer = j + 1;
                selected = true;
            }
        }
        res.release();
        return answer;
    }


    /**
     * 获取答题卡区域，去掉不要的部分
     *
     * @param src
     * @param img
     * @return
     */
    private Mat getAnsArea(Mat src, Mat img) {
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        List<Point> source = new ArrayList<>();
        // 寻找轮廓
        Mat hierarchy = new Mat();
        Imgproc.findContours(img, contours, hierarchy, RETR_LIST, CHAIN_APPROX_SIMPLE);

        // 找到矩形，宽度、长度分别大于原图 1/2 1/3
        int hw = img.cols() / 2;
        int hh = img.rows() / 4;
        Rect roi = new Rect();

        for (int i = 0; i < contours.size(); i++) {
            Rect rect = Imgproc.boundingRect(contours.get(i));
            if (rect.width > hw && rect.height > hh) {

                MatOfPoint2f c = new MatOfPoint2f(contours.get(i).toArray());
                // 计算周长
                double peri = Imgproc.arcLength(c, true);
                MatOfPoint2f approx = new MatOfPoint2f();
                // 计算轮廓找到四个顶点
                Imgproc.approxPolyDP(c, approx, peri * 0.01, true);

                // 画出找到的轮廓
                Imgproc.drawContours(src, contours, i, new Scalar(255, 0,0,1), 5);
//                Imgcodecs.imwrite(extStorageDirectory+ File.separator+"contour.jpg", src);

                if (approx.total() == 4) {
                    double[] temp = approx.get(0, 0);
                    Point p1 = new Point(temp[0], temp[1]);
                    temp = approx.get(1, 0);
                    Point p2 = new Point(temp[0], temp[1]);
                    temp = approx.get(2, 0);
                    Point p3 = new Point(temp[0], temp[1]);
                    temp = approx.get(3, 0);
                    Point p4 = new Point(temp[0], temp[1]);
                    source.add(p1);
                    source.add(p2);
                    source.add(p3);
                    source.add(p4);
                    // 确定roi区域
                    roi.x = rect.x;
                    roi.y = rect.y;
                    roi.width = rect.width;
                    roi.height = rect.height;

                    break;
                }
            }
        }

        if (roi.width == 0 || roi.height == 0) {
            return null;
        }

        //对获取的点进行排序， 点可能不止四个从里面选取四个方向的点
        Point centerPoint = new Point(0, 0);//质心
        for (Point corner : source) {
            centerPoint.x += corner.x;
            centerPoint.y += corner.y;
        }
        centerPoint.x = centerPoint.x / source.size();
        centerPoint.y = centerPoint.y / source.size();
        Point leftTop = new Point();
        Point rightTop = new Point();
        Point leftBottom = new Point();
        Point rightBottom = new Point();
        for (int i = 0; i < source.size(); i++) {
            if (source.get(i).x < centerPoint.x && source.get(i).y < centerPoint.y) {
                leftTop = source.get(i);
            } else if (source.get(i).x > centerPoint.x && source.get(i).y < centerPoint.y) {
                rightTop = source.get(i);
            } else if (source.get(i).x < centerPoint.x && source.get(i).y > centerPoint.y) {
                leftBottom = source.get(i);
            } else if (source.get(i).x > centerPoint.x && source.get(i).y > centerPoint.y) {
                rightBottom = source.get(i);
            }
        }
        source.clear();
        source.add(leftTop);
        source.add(rightTop);
        source.add(leftBottom);
        source.add(rightBottom);

        // 透视变换，裁剪图片只保留答题卡区域
        MatOfPoint2f cornerMat = new MatOfPoint2f(leftTop, rightTop, leftBottom, rightBottom);
        MatOfPoint2f quad = new MatOfPoint2f(new Point(roi.x, roi.y), new Point(roi.x + roi.width, roi.y),
                new Point(roi.x, roi.y + roi.height), new Point(roi.x + roi.width, roi.y + roi.height));

        Mat tran = Imgproc.getPerspectiveTransform(cornerMat, quad);

        Mat result = new Mat();
        Imgproc.warpPerspective(src, result, tran, result.size());
        result.submat(roi).copyTo(result);
//        Imgcodecs.imwrite(extStorageDirectory + File.separator + "sheetArea.jpg", result);

        // 释放资源
        hierarchy.release();
        tran.release();

        return result;
    }

    /**
     * 对图片进行预处理处理，包括，梯度化，高斯模糊，二值化，腐蚀，膨胀和边缘检测
     *
     * @param
     * @return
     */
    private Mat initImg(Mat src) {
        Mat dst = src.clone();
        Mat dstx = src.clone();
        Mat dsty = src.clone();

        Imgproc.GaussianBlur(dst, dst, new Size(3, 3), 0);
        Imgproc.Sobel(dst, dstx, -1, 1, 0, 3, 1, 0, Core.BORDER_DEFAULT);
        Imgproc.Sobel(dst, dsty, -1, 0, 1, 3, 1, 0, Core.BORDER_DEFAULT);
        Core.addWeighted(dstx, 5, dsty, 5, 0, dst);

//        Imgcodecs.imwrite(extStorageDirectory + File.separator + "sobel.jpg", dst);

        // OTSU二值化
        Mat otsu = new Mat();
        Imgproc.threshold(dst, otsu, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
//        Imgcodecs.imwrite(extStorageDirectory + File.separator + "threshold.jpg", otsu);


        // 膨胀,去掉边界干扰
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Imgproc.dilate(otsu, otsu, element);
//        Imgcodecs.imwrite(extStorageDirectory + File.separator + "dilate.jpg", otsu);

        // 释放资源
        dst.release();
        dstx.release();
        dsty.release();

        return otsu;
    }
}
