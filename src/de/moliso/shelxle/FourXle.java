package de.moliso.shelxle;


public class FourXle {
	static {
        System.loadLibrary("fourxle_kissfft");
    }
	//public static native FourXle(double resol, double wght);
	//public static float vert[] ;

	private static final int FLOAT_SIZE_BYTES = 4;
	float[] positions(GLES20TriangleRenderer mRenderer,Vector3D mid){		
		int siz = 3*(mRenderer.mol.size() + mRenderer.qmol.size());
		int j=0;
		float[] bb = new float[siz+3];
		bb[j++]=mid.x;
		bb[j++]=mid.y;
		bb[j++]=mid.z;
		for (int i=0; i<mRenderer.mol.size();i++){
			bb[j++]=mRenderer.mol.get(i).pos.x;
			bb[j++]=mRenderer.mol.get(i).pos.y;
			bb[j++]=mRenderer.mol.get(i).pos.z;
		}
		for (int i=0; i<mRenderer.qmol.size();i++){
			bb[j++]=mRenderer.qmol.get(i).pos.x;
			bb[j++]=mRenderer.qmol.get(i).pos.y;
			bb[j++]=mRenderer.qmol.get(i).pos.z;
		}
		return bb;
	}
	public native float[] loadFouAndPerform(String filename,boolean neu,
		    float[] p);
	public native float[] getFoPlus();
	public native float[] getFoMinus();
	public native float[] getDifPlus();
	public native float[] getDifMinus();
	public native void change_iso(int numsteps,int diff,
			float[] fopl, 
			float[] dimi, 
			float[] dipl);
}
