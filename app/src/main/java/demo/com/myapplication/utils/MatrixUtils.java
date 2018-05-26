package demo.com.myapplication.utils;

import android.opengl.Matrix;

/**
 * Created by LiuBin
 */
public final class MatrixUtils {
  //;
  public static final int TYPE_FITXY = 0;
  public static final int TYPE_CENTERCROP = 1;
  public static final int TYPE_CENTERINSIDE = 2;
  public static final int TYPE_FITSTART = 3;
  public static final int TYPE_FITEND = 4;

  @Deprecated
  public static void getShowMatrix(float[] matrix, int imgWidth, int imgHeight, int viewWidth,
      int viewHeight) {
    if (imgHeight > 0 && imgWidth > 0 && viewWidth > 0 && viewHeight > 0) {
      float sWhView = (float) viewWidth / viewHeight;
      float sWhImg = (float) imgWidth / imgHeight;
      float[] projection = new float[16];
      float[] camera = new float[16];
      if (sWhImg > sWhView) {
        Matrix.orthoM(projection, 0, -sWhView / sWhImg, sWhView / sWhImg, -1, 1, 1, 3);
      } else {
        Matrix.orthoM(projection, 0, -1, 1, -sWhImg / sWhView, sWhImg / sWhView, 1, 3);
      }
      Matrix.setLookAtM(camera, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0);
      Matrix.multiplyMM(matrix, 0, projection, 0, camera, 0);
    }
  }

  public static void getMatrix(float[] matrix, int type, int imgWidth, int imgHeight, int viewWidth,
      int viewHeight) {
    if (imgWidth <= 0 || imgHeight <= 0 || viewWidth <= 0 || viewHeight <= 0) return;
    float[] projection = new float[16];
    float[] camera = new float[16];
    if (type == TYPE_FITXY) {
      // 正交投影 所有多边形都是精确地按照指定的相对大小在屏幕上绘制的，线和多边形使用平行线来直接映射到2D屏幕上
      Matrix.orthoM(projection, 0, -1, 1, -1, 1, 1, 3);
      // 创建视图变换矩阵 camera
      Matrix.setLookAtM(camera, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0);
      // 模型变换 matrix -> projection 左乘 camera
      Matrix.multiplyMM(matrix, 0, projection, 0, camera, 0);
      return;
    }
    // view 的宽高比
    float sWhView = (float) viewWidth / viewHeight;
    // 图片的 宽高比, 越小于1 说明越窄， 越大于1 说明越宽
    float sWhImg = (float) imgWidth / imgHeight;
    // 如果图片相对于view宽
    if (sWhImg > sWhView) {
      switch (type) {
        case TYPE_CENTERCROP:
          Matrix.orthoM(projection, 0, -sWhView / sWhImg, sWhView / sWhImg, -1, 1, 1, 3);
          break;
        case TYPE_CENTERINSIDE:
          Matrix.orthoM(projection, 0, -1, 1, -sWhImg / sWhView, sWhImg / sWhView, 1, 3);
          break;
        case TYPE_FITSTART:
          Matrix.orthoM(projection, 0, -1, 1, 1 - 2 * sWhImg / sWhView, 1, 1, 3); // ???
          break;
        case TYPE_FITEND:
          Matrix.orthoM(projection, 0, -1, 1, -1, 2 * sWhImg / sWhView - 1, 1, 3); // ???
          break;
      }
    } else {
      switch (type) {
        case TYPE_CENTERCROP:
          Matrix.orthoM(projection, 0, -1, 1, -sWhImg / sWhView, sWhImg / sWhView, 1, 3);
          break;
        case TYPE_CENTERINSIDE:
          Matrix.orthoM(projection, 0, -sWhView / sWhImg, sWhView / sWhImg, -1, 1, 1, 3);
          break;
        case TYPE_FITSTART:
          Matrix.orthoM(projection, 0, -1, 2 * sWhView / sWhImg - 1, -1, 1, 1, 3);
          break;
        case TYPE_FITEND:
          Matrix.orthoM(projection, 0, 1 - 2 * sWhView / sWhImg, 1, -1, 1, 1, 3);
          break;
      }
    }
    Matrix.setLookAtM(camera, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0);
    Matrix.multiplyMM(matrix, 0, projection, 0, camera, 0);
  }

  public static void getCenterInsideMatrix(float[] matrix, int imgWidth, int imgHeight,
      int viewWidth, int viewHeight) {
    getMatrix(matrix, TYPE_CENTERINSIDE, imgWidth, imgHeight, viewWidth, viewHeight);
  }

  /**
   * 旋转
   * */
  public static float[] rotate(float[] m, float angle) {
    Matrix.rotateM(m, 0, angle, 0, 0, 1);
    return m;
  }

  public static float[] flip(float[] m,boolean x,boolean y){
    if(x||y){
      Matrix.scaleM(m,0,x?-1:1,y?-1:1,1);
    }
    return m;
  }

  /**
   * 缩放
   * */
  public static float[] scale(float[] m,float x,float y){
    Matrix.scaleM(m,0,x,y,1);
    return m;
  }

  public static float[] getOriginalMatrix(){
    return new float[]{
        1,0,0,0,
        0,1,0,0,
        0,0,1,0,
        0,0,0,1
    };
  }
}
