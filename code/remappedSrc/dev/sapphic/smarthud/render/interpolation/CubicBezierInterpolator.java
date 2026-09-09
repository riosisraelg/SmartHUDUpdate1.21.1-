package dev.sapphic.smarthud.render.interpolation;

/**
 * Derived from: https://github.com/codesoup/android-cubic-bezier-interpolator
 *
 * <p>Creates a cubic bezier curve based on two control points, and allows simple interpolation of a
 * 0..1 time value.
 */
public final class CubicBezierInterpolator implements Interpolator {
  private final float startX;
  private final float startY;
  private final float endX;
  private final float endY;

  private float v0x, v0y;
  private float v1x, v1y;
  private float v2x, v2y;

  public CubicBezierInterpolator(final float x0, final float y0, final float x1, final float y1) {
    if ((x0 < 0) || (x0 > 1)) {
      throw new IllegalArgumentException("start X value must be in the range [0, 1]");
    }

    if ((x1 < 0) || (x1 > 1)) {
      throw new IllegalArgumentException("end X value must be in the range [0, 1]");
    }

    this.startX = x0;
    this.startY = y0;
    this.endX = x1;
    this.endY = y1;
  }

  @Override
  public float interpolate(final float time) {
    return this.getBezierCoordinateY(this.getXForTime(time));
  }

  private float getBezierCoordinateY(final float time) {
    this.v2y = 3 * this.startY;
    this.v1y = (3 * (this.endY - this.startY)) - this.v2y;
    this.v0y = 1 - this.v2y - this.v1y;

    return time * (this.v2y + (time * (this.v1y + (time * this.v0y))));
  }

  private float getXForTime(final float time) {
    float x = time;

    for (int i = 1; i < 14; ++i) {
      final float z = this.getBezierCoordinateX(x) - time;

      if (Math.abs(z) < 0.001) {
        return x;
      }

      x -= z / this.getSlope(x);
    }

    return x;
  }

  @SuppressWarnings("OverlyComplexArithmeticExpression")
  private float getSlope(final float time) {
    return this.v2x + (time * ((2 * this.v1x) + (3 * this.v0x * time)));
  }

  private float getBezierCoordinateX(final float time) {
    this.v2x = 3 * this.startX;
    this.v1x = (3 * (this.endX - this.startX)) - this.v2x;
    this.v0x = 1 - this.v2x - this.v1x;

    return time * (this.v2x + (time * (this.v1x + (time * this.v0x))));
  }
}
