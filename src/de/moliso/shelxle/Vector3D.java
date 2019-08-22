package de.moliso.shelxle;

public class Vector3D {
	public float x;
	public float y;
	public float z;

	public Vector3D(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector3D() {
		this.x = 0;
		this.y = 0;
		this.z = 0;
	}

	public Vector3D(float[] v) {
		if (v.length == 3) {
			this.x = v[0];
			this.y = v[1];
			this.z = v[2];
		} else {
			this.x = 0;
			this.y = 0;
			this.z = 0;
		}
	}

	public float[] toArray() {
		float[] a = new float[3];
		a[0] = this.x;
		a[1] = this.y;
		a[2] = this.z;
		return a;
	}

	public Vector3D add(Vector3D other) {
		this.x += other.x;
		this.y += other.y;
		this.z += other.z;
		return this;
	}

	public Vector3D zero() {
		this.x = 0;
		this.y = 0;
		this.z = 0;
		return this;
	}

	public Vector3D add(float a, float b, float c) {
		this.x += a;
		this.y += b;
		this.z += c;
		return this;
	}

	public Vector3D subtract(Vector3D other) {
		this.x -= other.x;
		this.y -= other.y;
		this.z -= other.z;
		return this;
	}

	public Vector3D scale(float s) {
		this.x *= s;
		this.y *= s;
		this.z *= s;
		return this;
	}

	public Vector3D sum(Vector3D a1, Vector3D a2) {
		this.x = a1.x + a2.x;
		this.y = a1.y + a2.y;
		this.z = a1.z + a2.z;
		return this;
	}

	public static Vector3D dif(Vector3D a1, Vector3D a2) {
		Vector3D his = new Vector3D();
		his.x = a1.x - a2.x;
		his.y = a1.y - a2.y;
		his.z = a1.z - a2.z;
		return his;
	}

	public float dot(Vector3D other) {
		float d = this.x * other.x + this.y * other.y + this.z * other.z;
		return d;
	}

	public static float dot(Vector3D a1, Vector3D a2) {
		float d = a1.x * a2.x + a1.y * a2.y + a1.z * a2.z;
		return d;
	}

	public static Vector3D mmult(float m[], Vector3D o) {
		Vector3D t = new Vector3D(0f, 0f, 0f);
		t.x = m[0] * o.x + m[1] * o.y + m[2] * o.z;
		t.y = m[3] * o.x + m[4] * o.y + m[5] * o.z;
		t.z = m[6] * o.x + m[7] * o.y + m[8] * o.z;
		return t;
	}

	public Vector3D cross(Vector3D a1, Vector3D a2) {
		Vector3D t = new Vector3D(0f, 0f, 0f);
		t.x = a1.y * a2.z - a2.y * a1.z;
		t.y = a1.z * a2.x - a2.z * a1.x;
		t.z = a1.x * a2.y - a2.x * a1.y;
		return t;
	}

	public float norm() {
		return this.x * this.x + this.y * this.y + this.z * this.z;
	}

	public float length() {
		return (float) Math.sqrt(norm());
	}

	public static float norm(Vector3D other) {
		return other.x * other.x + other.y * other.y + other.z * other.z;
	}

	public Vector3D normalized() {
		float d = (float) Math.sqrt(this.norm());
		if (d != 0.0f)
			this.scale(1.0f / d);
		return this;
	}

	public Vector3D normalize(Vector3D that) {
		float d = (float) Math.sqrt(that.norm());
		if (d != 0.0f)
			that.scale(1.0f / d);
		return that;
	}

	public static double distance(Vector3D a1, Vector3D a2) {
		double d = Math.sqrt(norm(dif(a1, a2)));
		return d;
	}

	public static double winkel(Vector3D a, Vector3D b) {
		double erg;
		if ((norm(a) < 0.1) || (norm(b) < 0.1))
			return 0;
		erg = dot(a, b) / (Math.sqrt(norm(a)) * Math.sqrt(norm(b)));
		// erg=(a.x*b.x+a.y*b.y+a.z*b.z)/(sqrt(a.x*a.x+a.y*a.y+a.z*a.z)*sqrt(b.x*b.x+b.y*b.y+b.z*b.z));
		erg = Math.acos(erg) / Math.PI * 180.0;
		return (erg);
	}

};
