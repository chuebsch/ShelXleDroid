package de.moliso.shelxle;

import java.lang.String;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import android.util.Log;

import de.moliso.shelxle.Vector3D;

class SDM {
	int a1,a2,sn;
	float d;
	Vector3D floorD;
	SDM(){
		a1=a2=sn=0;
		d=0;
		floorD=new Vector3D(0,0,0);
	}
};

public class Molecule {
	public Vector3D pos = new Vector3D(0, 0, 0), frac = new Vector3D(0, 0, 0);
	public float rad;
	public float phgt;
	public float iso;
	public double sof;
	public double sof_org;
	public float[] screenXY = new float[2];
	public Vector3D ev = new Vector3D(0, 0, 0);
	public Vector3D col = new Vector3D(0, 0, 0);
	public String lab;
	public int an, afix, part, resiNr, afixParent, molindex;
	public String ResiClass;
	public float[] uc = new float[9];
	public float[] uf;// =new float[9];
	public float ang;
	public int symmGroup = 0;
	public Vector3D ax = new Vector3D(0, 0, 0);
	static public final float[][] atomColor = {
			{ 0.972549f, 0.972549f, 0.972549f },// 0 H
			{ 0.627451f, 0.752941f, 0.623529f },// 1 HE
			{ 0.501961f, 0.000000f, 0.000000f },// 2 LI
			{ 0.752941f, 0.752941f, 0.752941f },// 3 BE
			{ 0.596078f, 0.337255f, 0.113725f },// 4 B
			{ 0.423529f, 0.431373f, 0.447059f },// 5 C
			{ 0.000000f, 0.000000f, 1.000000f },// 6 N
			{ 1.000000f, 0.000000f, 0.000000f },// 7 O
			{ 0.498039f, 1.000000f, 0.000000f },// 8 F
			{ 0.752941f, 0.752941f, 0.752941f },// 9 NE
			{ 0.937255f, 0.603922f, 0.984314f },// 10 NA
			{ 0.200000f, 0.600000f, 1.000000f },// 11 MG
			{ 0.400000f, 0.701961f, 1.000000f },// 12 AL
			{ 0.601961f, 0.601961f, 0.601961f },// 13 SI
			{ 0.717647f, 0.160784f, 0.956863f },// 14 P
			{ 0.745098f, 0.745098f, 0.000000f },// 15 S
			{ 0.254902f, 0.435294f, 0.254902f },// 16 CL
			{ 0.752941f, 0.752941f, 0.752941f },// 17 AR
			{ 0.752941f, 0.180392f, 0.705882f },// 18 K
			{ 0.666667f, 0.666667f, 0.501961f },// 19 CA
			{ 0.925490f, 0.533333f, 0.670588f },// 20 SC
			{ 0.849020f, 0.331373f, 0.011765f },// 21 TI
			{ 0.392157f, 0.501961f, 0.000000f },// 22 V
			{ 1.000000f, 1.000000f, 0.000000f },// 23 CR
			{ 0.800000f, 0.600000f, 0.600000f },// 24 MN
			{ 0.000000f, 0.635294f, 0.000000f },// 25 FE
			{ 0.427451f, 0.000000f, 0.427451f },// 26 CO
			{ 0.666667f, 0.321569f, 0.250980f },// 27 NI
			{ 0.980392f, 0.501961f, 0.447059f },// 28 CU
			{ 0.247059f, 0.482353f, 0.435294f },// 29 ZN
			{ 0.305882f, 0.996078f, 0.815686f },// 30 GA
			{ 0.533333f, 0.533333f, 0.533333f },// 31 GE
			{ 0.749020f, 0.854902f, 1.000000f },// 32 AS
			{ 0.352941f, 0.090196f, 0.090196f },// 33 SE
			{ 0.870588f, 0.737255f, 0.521569f },// 34 BR
			{ 0.941176f, 0.470588f, 0.000000f },// 35 KR
			{ 0.501961f, 0.000000f, 0.501961f },// 36 RB
			{ 0.752941f, 0.752941f, 0.752941f },// 37 SR
			{ 0.800000f, 0.443137f, 0.125490f },// 38 Y
			{ 0.666667f, 0.333333f, 0.498039f },// 39 ZR
			{ 0.752941f, 0.752941f, 0.752941f },// 40 NB
			{ 0.752941f, 0.752941f, 0.752941f },// 41 MO
			{ 0.752941f, 0.752941f, 0.752941f },// 42 TC
			{ 0.752941f, 0.752941f, 0.752941f },// 43 RU
			{ 0.752941f, 0.752941f, 0.752941f },// 44 RH
			{ 0.752941f, 0.752941f, 0.752941f },// 45 PD
			{ 0.752941f, 0.752941f, 0.752941f },// 46 AG
			{ 0.752941f, 0.752941f, 0.752941f },// 47 CD
			{ 0.752941f, 0.752941f, 0.752941f },// 48 IN
			{ 0.752941f, 0.752941f, 0.752941f },// 49 SN
			{ 0.752941f, 0.752941f, 0.752941f },// 50 SB
			{ 0.752941f, 0.752941f, 0.752941f },// 51 TE
			{ 0.588235f, 0.094118f, 0.752941f },// 52 I
			{ 0.752941f, 0.752941f, 0.752941f },// 53 XE
			{ 0.752941f, 0.752941f, 0.752941f },// 54
			{ 0.752941f, 0.752941f, 0.752941f },// 55
			{ 0.752941f, 0.752941f, 0.752941f },// 56
			{ 0.752941f, 0.752941f, 0.752941f },// 57
			{ 0.752941f, 0.752941f, 0.752941f },// 58
			{ 0.752941f, 0.752941f, 0.752941f },// 59
			{ 0.752941f, 0.752941f, 0.752941f },// 60
			{ 0.752941f, 0.752941f, 0.752941f },// 61
			{ 0.752941f, 0.752941f, 0.752941f },// 62
			{ 0.752941f, 0.752941f, 0.752941f },// 63
			{ 0.752941f, 0.752941f, 0.752941f },// 64
			{ 0.752941f, 0.752941f, 0.752941f },// 65
			{ 0.752941f, 0.752941f, 0.752941f },// 66
			{ 0.752941f, 0.752941f, 0.752941f },// 67
			{ 0.752941f, 0.752941f, 0.752941f },// 68
			{ 0.752941f, 0.752941f, 0.752941f },// 69
			{ 0.752941f, 0.752941f, 0.752941f },// 70
			{ 0.752941f, 0.752941f, 0.752941f },// 71
			{ 0.752941f, 0.752941f, 0.752941f },// 72
			{ 0.403922f, 0.435294f, 0.623529f },// 73
			{ 0.752941f, 0.752941f, 0.752941f },// 74
			{ 0.752941f, 0.752941f, 0.752941f },// 75
			{ 0.752941f, 0.752941f, 0.752941f },// 76
			{ 0.752941f, 0.752941f, 0.752941f },// 77
			{ 0.854902f, 0.647059f, 0.125490f },// 78
			{ 0.752941f, 0.752941f, 0.752941f },// 79
			{ 0.752941f, 0.752941f, 0.752941f },// 80
			{ 0.752941f, 0.752941f, 0.752941f },// 81
			{ 0.752941f, 0.752941f, 0.752941f },// 82
			{ 0.752941f, 0.752941f, 0.752941f },// 83
			{ 0.752941f, 0.752941f, 0.752941f },// 84
			{ 0.752941f, 0.752941f, 0.752941f },// 85
			{ 0.752941f, 0.752941f, 0.752941f },// 86
			{ 0.752941f, 0.752941f, 0.752941f },// 87
			{ 0.752941f, 0.494118f, 0.231373f },// 88
			{ 0.752941f, 0.752941f, 0.752941f },// 89
			{ 0.752941f, 0.752941f, 0.752941f },// 90
			{ 0.752941f, 0.752941f, 0.752941f },// 91
			{ 0.752941f, 0.752941f, 0.752941f },// 92
			{ 0.752941f, 0.752941f, 0.752941f },// 93
			{ 0.752941f, 0.752941f, 0.752941f },// 94
			{ 0.752941f, 0.752941f, 0.752941f },// 95
			{ 0.752941f, 0.752941f, 0.752941f },// 96
			{ 0.752941f, 0.752941f, 0.752941f },// 97
			{ 0.752941f, 0.752941f, 0.752941f },// 98
			{ 0.752941f, 0.752941f, 0.752941f },// 99
			{ 0.752941f, 0.752941f, 0.752941f },// 100
			{ 0.752941f, 0.752941f, 0.752941f },// 101
			{ 0.752941f, 0.752941f, 0.752941f },// 102
			{ 0.752941f, 0.752941f, 0.752941f },// 103
			{ 0.752941f, 0.752941f, 0.752941f },// 104
			{ 0.752941f, 0.752941f, 0.752941f },// 105
			{ 0.752941f, 0.752941f, 0.752941f },// 106
			{ 0.752941f, 0.752941f, 0.752941f },// 107
			{ 0.752941f, 0.752941f, 0.752941f } // 108
	};

	/*
	 * 
	 * #define M(row,col) m[col*4+row] out[0] = M(0, 0) * in[0] + M(0, 1) *
	 * in[1] + M(0, 2) * in[2] + M(0, 3) * in[3]; out[1] = M(1, 0) * in[0] +
	 * M(1, 1) * in[1] + M(1, 2) * in[2] + M(1, 3) * in[3]; out[2] = M(2, 0) *
	 * in[0] + M(2, 1) * in[1] + M(2, 2) * in[2] + M(2, 3) * in[3]; out[3] =
	 * M(3, 0) * in[0] + M(3, 1) * in[1] + M(3, 2) * in[2] + M(3, 3) * in[3];
	 * #undef M
	 */
	static void transform_point(float out[], final float m[], final float in[]) {
		out[0] = m[0] * in[0] + m[4] * in[1] + m[8] * in[2] + m[12] * in[3];
		out[1] = m[1] * in[0] + m[5] * in[1] + m[9] * in[2] + m[13] * in[3];
		out[2] = m[2] * in[0] + m[6] * in[1] + m[10] * in[2] + m[14] * in[3];
		out[3] = m[3] * in[0] + m[7] * in[1] + m[11] * in[2] + m[15] * in[3];
	}

	static float in[] = new float[4];
	static float out[] = new float[4];

	static boolean posTo2D(Vector3D obj, final float pmv[],
			final int viewport[], float win[]) {
	    
		in[0] = obj.x;
		in[1] = obj.y;
		in[2] = obj.z;
		in[3] = 1.0f;
		transform_point(out, pmv, in);

		if (out[3] == 0.0)
			return false;

		out[0] /= out[3];
		out[1] /= out[3];
		out[2] /= out[3];

		win[0] = viewport[0] + (1 + out[0]) * viewport[2] / 2;
		win[1] = viewport[1] + (1 - out[1]) * viewport[3] / 2;
		return true;

	}

	public Molecule() {

	}

	public Molecule(Molecule m) {
		pos = m.pos;
		frac = m.frac;
		rad = m.rad;
		ev = m.ev;
		col = m.col;
		lab = m.lab;
		an = m.an;
		afix = m.afix;
		part = m.part;
		resiNr = m.resiNr;
		afixParent = m.afixParent;
		ResiClass = m.ResiClass;
		symmGroup = m.symmGroup;
		System.arraycopy(uc, 0, m.uc, 0, 9);
		System.arraycopy(uf, 0, m.uf, 0, 9);
		ang = m.ang;
		ax = m.ax;

	}

	public Molecule(Vector3D p, float r, Vector3D c, String s) {
		pos = p;
		rad = r;
		lab = s;
		col = c;
	}

	Molecule(Vector3D p, int oz, String s) {
		an = oz;
		pos = p;
		lab = s;
		col = new Vector3D(0.3f, 0.3f, 0.3f);
	};

	Molecule(String s, Vector3D p, int oz, float[] ufr) {
		pos = p;
		lab = s;
		an = oz;
		System.arraycopy(ufr, 0, uf, 0, 9);
		col = new Vector3D(0.3f, 0.3f, 0.3f);
	};
    static public int maxmol = 0;
     
	static public ArrayList<String> sdm(Cell cell, ArrayList<Molecule> asymm) {
		float min = 10000;
		SDM sdmItem;
	   sdmItem=new SDM();
			
		ArrayList<SDM> sdm = new ArrayList<SDM>();
		float h = 0, k = 0, l = 0;
		ArrayList<String> brauchSymm = new ArrayList<String>();
		int s = 0;
		Vector3D prime, D, floorD, dp, half = new Vector3D(0.5f, 0.5f,
				0.5f);
		for (int i=0; i<asymm.size(); i++){
		    for (int j=0; j<asymm.size(); j++ ){
		      boolean hma=false;
		      min=1000000;
		      for (int n=0;n<cell.symmops.size();  n++){
		    	  prime = Vector3D.mmult(cell.symmops.get(n),
							asymm.get(j).frac);
					prime.add(cell.trans.get(n));
					D = new Vector3D(asymm.get(i).frac.x, asymm.get(i).frac.y,
							asymm.get(i).frac.z);
					D.subtract(prime);
					D.add(half);
					floorD = new Vector3D((float) Math.floor(D.x),
							(float) Math.floor(D.y), (float) Math.floor(D.z));
					dp = new Vector3D(D.x, D.y, D.z);
					dp.subtract(floorD);
					dp.subtract(half);
					float dk = cell.fl(dp.x, dp.y, dp.z);

		        if ((dk>0.01)&&((min+0.05)>=dk)){
			  min=dk;
			  sdmItem=new SDM();
			  sdmItem.d=min;
			  sdmItem.floorD=floorD;
			  sdmItem.a1=i;
			  sdmItem.a2=j;
			  sdmItem.sn=n;
			  hma=true;
			}
		      }
		      float dddd;
		      if ((!asymm.isEmpty())&&(asymm.get(sdmItem.a1).an>-1)&&(asymm.get(sdmItem.a2).an>-1)&&
		((asymm.get(sdmItem.a1).part*asymm.get(sdmItem.a2).part==0)||
		(asymm.get(sdmItem.a1).part==asymm.get(sdmItem.a2).part)))
			 dddd=(Kovalenz_Radien[asymm.get(sdmItem.a1).an]+ Kovalenz_Radien[asymm.get(sdmItem.a2).an])*0.013f;
		      else dddd=0;
		      if ((sdmItem.d<dddd)&&(hma)) 
		    	  sdm.add(sdmItem);
		    }
		  }
		
		
		// Vector3D mid = new Vector3D();
		// Vector3D DD=new Vector3D();
		int someleft = 0, nextmol = 0;
		maxmol = 1;
		for (int i = 0; i < asymm.size(); i++)
			asymm.get(i).molindex = -1;
		if (!sdm.isEmpty()) asymm.get(sdm.get(0).a1).molindex = 1;// starter;
		do {
			nextmol = 0;
			do {
				someleft = 0;
				for (int i = 0; i < sdm.size(); i++) {
					if ((asymm.get(sdm.get(i).a1).molindex
									* asymm.get(sdm.get(i).a2).molindex) < 0) {
						asymm.get(sdm.get(i).a1).molindex = maxmol;
						asymm.get(sdm.get(i).a2).molindex = maxmol;
						//Log.d("SDM",asymm.get(sdm.get(i).a1).lab+"=="+asymm.get(sdm.get(i).a2).lab+" "+maxmol);
						someleft++;
					}
				}
			} while (someleft != 0);
			for (int i = 0; i < asymm.size(); i++) {
				if ((asymm.get(i).an > -1) && (asymm.get(i).molindex < 0)) {
					nextmol = i;
					break;
				}
			}
			if (nextmol != 0) {
				asymm.get(nextmol).molindex = (++maxmol);
			}
		} while (nextmol != 0);
		//Log.d("SDM","maxmol="+maxmol+" sdm size"+sdm.size());
		//asymm[sdm.at(k).a1].molindex
		//for (int i = 0; i < asymm.size(); i++) {
			//for (int j = 0; j < asymm.size(); j++) {
				//if ((asymm.get(i).an < 0) || (asymm.get(j).an < 0))
				//	continue;
		for (int ki=0; ki<sdm.size();ki++){
				min = 0.0125f * (Molecule.Kovalenz_Radien[asymm.get(sdm.get(ki).a1).an] + 
						Molecule.Kovalenz_Radien[asymm
						.get(sdm.get(ki).a2).an]);
				// mid.add(asymm.get(i).frac);
				// }
				// mid.scale(1.0f / asymm.size());
				//Vector3D prime, D, floorD, dp, half = new Vector3D(0.5f, 0.5f,0.5f);
				for (int n = 1; n < cell.symmops.size(); n++) {
					prime = Vector3D.mmult(cell.symmops.get(n),
							asymm.get(sdm.get(ki).a1).frac);
					prime.add(cell.trans.get(n));
					D = new Vector3D(asymm.get(sdm.get(ki).a2).frac.x, asymm.get(sdm.get(ki).a2).frac.y,
							asymm.get(sdm.get(ki).a2).frac.z);
					D.subtract(prime);
					D.add(half);
					floorD = new Vector3D((float) Math.floor(D.x),
							(float) Math.floor(D.y), (float) Math.floor(D.z));
					dp = new Vector3D(D.x, D.y, D.z);
					dp.subtract(floorD);
					dp.subtract(half);
					float dk = cell.fl(dp.x, dp.y, dp.z);
					if (dk < min) {
						// DD=new Vector3D(D.x,D.y,D.z);
						h = floorD.x + 5;
						k = floorD.y + 5;
						l = floorD.z + 5;
						s = n + 1;
						String bs = String.format(Locale.US,
								"%1d_%1.0f%1.0f%1.0f:%d", s, h, k, l,asymm.get(sdm.get(ki).a1).molindex);
						if (!brauchSymm.contains(bs)){
							brauchSymm.add(bs);	
						    //Log.d("SDM", String.format(Locale.US,
							//		"%1d_%1.0f%1.0f%1.0f:%d %f %f", s, h, k, l,asymm.get(sdm.get(ki).a1).molindex,dk,min));
						}

						

					}
					min = Math.min(dk, min);
				}
			}
		//}
		// Log.d("SDM",String.format(Locale.US,"%1.0f %1.0f %1.0f %f\n %1.0f %1.0f %1.0f %f\n%1.0f %1.0f %1.0f %f\n",
		// cell.symmops.get(s-1)[0],
		// cell.symmops.get(s-1)[1],
		// cell.symmops.get(s-1)[2],
		// cell.trans.get(s-1).x,
		// cell.symmops.get(s-1)[3],
		// cell.symmops.get(s-1)[4],
		// cell.symmops.get(s-1)[5],
		// cell.trans.get(s-1).y,
		// cell.symmops.get(s-1)[6],
		// cell.symmops.get(s-1)[7],
		// cell.symmops.get(s-1)[8],
		// cell.trans.get(s-1).z
		//
		// ));
		// Log.d("SDM",
		// String.format(Locale.US,"%g %g %g\n",DD.x,DD.y,DD.z));
		return brauchSymm;
	}

	public ArrayList<Molecule> packer(ArrayList<String> brauchSymm,
			ArrayList<Molecule> asymm, Cell cell) {
		/*
		 * ! Packs symmetry equivalent atoms according to the given list of
		 * internal symmetry codes and adds them to the showatoms list
		 * HumanSymmetry is feeeded with a human readalble list of used
		 * symmetry.
		 * 
		 * @param brauchSymm list of internal symmetry codes.
		 */
		@SuppressWarnings("unchecked")
		ArrayList<Molecule> showatoms = (ArrayList<Molecule>) asymm.clone();
		int s = 1, h = 5, k = 5, l = 5, symmgroup = 0;
		boolean gibscho = false;
		// String pre, suff;
		// 2_656:1
		// 3_656:2
		for (int j = 0; j < brauchSymm.size(); j++) {
			Pattern p = Pattern
					.compile("([0-9]+)\\_([0-9]{1})([0-9]{1})([0-9]{1})\\:([0-9]+)");
			Matcher m = p.matcher(brauchSymm.get(j));
			if (m.matches()) {
				s = (int) Integer.parseInt(m.group(1));
				h = (int) Integer.parseInt(m.group(2));
				k = (int) Integer.parseInt(m.group(3));
				l = (int) Integer.parseInt(m.group(4));

				symmgroup = Integer.parseInt(m.group(5));
			}
			// sscanf(brauchSymm.get(j),"%d_%1d%1d%1d:%d",&s,&h,&k,&l,&symmgroup);
			// printf("BS:!%s! %d h%d k%d l%d sg%d\n",brauchSymm.at(j).toStdString().c_str(),s,h,k,l,symmgroup);
			h -= 5;
			k -= 5;
			l -= 5;
			s--;

			// Log.d("symmm:", String.valueOf(h) + " " + k + " " + l + " " + s);
			for (int i = 0; i < asymm.size(); i++) {
				Molecule newAtom = new Molecule(new Vector3D(0F, 0F, 0F), -1,
						"X");
				// if (asymm[i].molindex) printf
				// ("doch %d %d %s\n",asymm[i].an,asymm[i].molindex,asymm.at(i).Label.toStdString().c_str());
				if (
			    (asymm.get(i).molindex==symmgroup)&&
				(asymm.get(i).an > -1)) {
					// Log.d("symmm:",String.format(Locale.US,"%f %f %f   %f\n%f %f %f  %f\n%f %f %f   %f\n",
					// cell.symmops.get(s)[0],
					// cell.symmops.get(s)[1],
					// cell.symmops.get(s)[2],
					// cell.trans.get(s).x,
					// cell.symmops.get(s)[3],
					// cell.symmops.get(s)[4],
					// cell.symmops.get(s)[5],
					// cell.trans.get(s).y,
					// cell.symmops.get(s)[6],
					// cell.symmops.get(s)[7],
					// cell.symmops.get(s)[8],
					// cell.trans.get(s).z));
					newAtom.frac = Vector3D.mmult(cell.symmops.get(s),
							asymm.get(i).frac);
					newAtom.frac.add(cell.trans.get(s));
					newAtom.frac.add(h, k, l);
					newAtom.part = asymm.get(i).part;
					newAtom.pos = cell.frac2cart(newAtom.frac);
					newAtom.lab = asymm.get(i).lab + ":"
							+ String.valueOf(j + 1);// */
					newAtom.an = asymm.get(i).an;
					newAtom.rad = asymm.get(i).rad;
					newAtom.symmGroup = j + 1;
					/*
					 * newAtom.sof_org=asymm.get(i).sof_org;
					 * newAtom.molindex=asymm.get(i).molindex;
					 * newAtom.ResiClass=asymm.get(i).ResiClass;
					 * newAtom.resiNr=asymm.get(i).resiNr;
					 */
					if ((asymm.get(i).uc[1] == 0.0)
							&& (asymm.get(i).uc[2] == 0.0)
							&& (asymm.get(i).uc[5] == 0.0)) {
						newAtom.uc = Cell
								.matrix(asymm.get(i).uf[0], 0, 0, 0,
										asymm.get(i).uf[0], 0, 0, 0,
										asymm.get(i).uf[0]);
						newAtom.uf = new float[9];
						System.arraycopy(asymm.get(i).uf, 0, newAtom.uf, 0, 9);
					} else {
						newAtom.uf = cell.Usym(asymm.get(i).uf,
								cell.symmops.get(s));
						cell.uFrac2UCart(newAtom.uf, newAtom.uc);
					}
					gibscho = false;
					if (newAtom.part >= 0) {
						for (int gbt = 0; gbt < showatoms.size(); gbt++) {
							if (showatoms.get(gbt).an < 0)
								continue;
							if (cell.fl(newAtom.frac.x
									- showatoms.get(gbt).frac.x, newAtom.frac.y
									- showatoms.get(gbt).frac.y, newAtom.frac.z
									- showatoms.get(gbt).frac.z) < 0.2f)
								gibscho = true;
						}
					}
					if (!gibscho) {
						showatoms.add(newAtom);
					}
				}// /*

			}
			// return showatoms;
		}
		// statusBar()->showMessage(tr("Neighbor search is finished"));
		return showatoms;

	}

	static public final short[] Kovalenz_Radien = { 55, 1, 123, 90, 80, 77, 74,
			71, 72, 1, 154, 149, 118, 111, 106, 102, 99, 1, 203, 174, 144, 132,
			122, 118, 117, 117, 116, 124, 117, 125, 126, 122, 120, 116, 114, 1,
			218, 191, 162, 145, 134, 130, 127, 125, 125, 128, 134, 148, 144,
			141, 140, 136, 133, 1, 235, 198, 169, 165, 165, 164, 164, 162, 185,
			161, 159, 159, 157, 157, 156, 170, 156, 144, 134, 130, 128, 126,
			127, 130, 134, 149, 148, 147, 146, 146, 145, 1, 0, 1, 188, 165,
			161, 142, 130, 151, 182 };
	static private final String[] PSE = { "H", "HE", "LI", "BE", "B", "C", "N",
			"O", "F", "NE", "NA", "MG", "AL", "SI", "P", "S", "CL", "AR", "K",
			"CA", "SC", "TI", "V", "CR", "MN", "FE", "CO", "NI", "CU", "ZN",
			"GA", "GE", "AS", "SE", "BR", "KR", "RB", "SR", "Y", "ZR", "NB",
			"MO", "TC", "RU", "RH", "PD", "AG", "CD", "IN", "SN", "SB", "TE",
			"I", "XE", "CS", "BA", "LA", "CE", "PR", "ND", "PM", "SM", "EU",
			"GD", "TB", "DY", "HO", "ER", "TM", "YB", "LU", "HF", "TA", "W",
			"RE", "OS", "IR", "PT", "AU", "HG", "TL", "PB", "BI", "PO", "AT",
			"RN", "FR", "RA", "AC", "TH", "PA", "U", "NP", "PU", "AM", "CM",
			"BK", "CF", "ES", "FM", "MD", "NO", "LR", "KU", "HA", "RF", "NS",
			"HS", "MT" };

	static public int getOZ(String S1) {
		String s = S1.split("[^A-Za-z]")[0].trim();
		s = s.toUpperCase(Locale.US);

		if (s.compareTo("CNT") == 0) {
			return -66;
		} else if (s.compareTo("D") == 0) {
			return 0;
		}
		for (int i = 0; i < PSE.length; i++) {
			if (s.compareTo(PSE[i]) == 0) {
				return i;
			}
		}
		return -1;
	}
	// private int index;
}
