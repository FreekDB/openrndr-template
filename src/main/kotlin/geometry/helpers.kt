package geometry

import org.openrndr.math.Vector2

/*
PVector intersectRays(Ray a, Ray b) {
  PVector d = PVector.sub(b.start, a.start);
  float det = b.direction.x * a.direction.y - b.direction.y * a.direction.x;
  if (det != 0) {
    float u = (d.y * b.direction.x - d.x * b.direction.y) / det;
    float v = (d.y * a.direction.x - d.x * a.direction.y) / det;
    if (u > 0 && v > 0) {
      // front side
      fill(255);
      return PVector.add(a.start, PVector.mult(a.direction, u));
    }
    if (u < 0 && v < 0) {
      // backside
      fill(#FF0000);
      return PVector.add(a.start, PVector.mult(a.direction, u));
    }
  }
  return new PVector(); // Deal with this later
}
*/