package de.moliso.shelxle;

import java.util.ArrayList;
//import java.util.HashMap;
import java.util.Locale;

public class Cell{

    final private double g2r = (180.0/Math.PI);

    Cell(float A, float B, float C, float alpha, float beta, float gamma, float Lambda){
    	a=A;
        b=B;
        c=C;
        al=alpha;
        be=beta;
        ga=gamma;
        wave=Lambda;

        cosal=(float)Math.cos((double)al/g2r);
        cosbe=(float)Math.cos((double)be/g2r);
        cosga=(float)Math.cos((double)ga/g2r);
        singa=(float)Math.sin((double)ga/g2r);
        tanga=(float)Math.tan((double)ga/g2r);

        phi= (float) Math.sqrt(1-(cosal*cosal)-
        		(cosbe*cosbe)-(cosga*cosga)
        		+2*cosal*cosbe*cosga);
        tau=c *((cosal- cosbe* cosga)/ singa);
        V = a*b*c*phi;
        as=c*b*(float) Math.sin(al/g2r)/V;
        bs=c*a*(float) Math.sin(be/g2r)/V;
        cs=a*b*singa/V;
    }
    Vector3D frac2cart(Vector3D x){
    	Vector3D y=new Vector3D(0f,0f,0f);
    	float[] u=new float[9];
    	u[0]=a;
    	u[1]=b*cosga;
    	u[2]=c*cosbe;
    	
    	u[3]=0;
    	u[4]=b*singa;
    	u[5]=tau;
    	
    	u[6]=0;
    	u[7]=0;
    	u[8]=c*phi/singa;
    	y.x=x.x*u[0]+x.y*u[1]+x.z*u[2];
    	y.y=x.x*u[3]+x.y*u[4]+x.z*u[5];
    	y.z=x.x*u[6]+x.y*u[7]+x.z*u[8];
    	
        return y;
    }
    //QMatrix4x4
    void mmult(float[] res, float[] m, float[] n){
    	res[0]=m[0]*n[0]+m[1]*n[3]+m[2]*n[6];
    	res[1]=m[0]*n[1]+m[1]*n[4]+m[2]*n[7];
    	res[2]=m[0]*n[2]+m[1]*n[5]+m[2]*n[8];
    	res[3]=m[3]*n[0]+m[4]*n[3]+m[5]*n[6];
    	res[4]=m[3]*n[1]+m[4]*n[4]+m[5]*n[7];
    	res[5]=m[3]*n[2]+m[4]*n[5]+m[5]*n[8];
    	res[6]=m[6]*n[0]+m[7]*n[3]+m[8]*n[6];
    	res[7]=m[6]*n[1]+m[7]*n[4]+m[8]*n[7];
    	res[8]=m[6]*n[2]+m[7]*n[5]+m[8]*n[8];
    }
    void uFrac2UCart(float[] frac, float[] cart){
    	float[] A=new float[9];
    	float[] o=new float[9];

    	float[] ot=new float[9];
    	float[] v=new float[9];
    	float[] w=new float[9];
    	A[0]=as;
    	A[1]=0.0f;
    	A[2]=0.0f;
    	
    	A[3]=0.0f;
    	A[4]=bs;
    	A[5]=0.0f;
    	
    	
        A[6]=0.0f;
    	A[7]=0.0f;
    	A[8]=cs;

    	ot[0]=o[0]=a;
    	ot[3]=o[1]=0.0f;
    	ot[6]=o[2]=0.0f;
    	     
    	ot[1]=o[3]=b*cosga;
    	ot[4]=o[4]=b*singa;
    	ot[7]=o[5]=0.0f;
    	     
        ot[2]=o[6]=c*cosbe;
    	ot[5]=o[7]=tau;
    	ot[8]=o[8]=c*phi/singa;
    	mmult(v,A,frac);
    	mmult(w,v,A);
    	mmult(v,o,w);
    	mmult(cart,v,ot);
    }
    
    public float[] transponse(float[] m){
    	float y[]=new float[9];
    	y[0]=m[0];
    	y[1]=m[3];
    	y[2]=m[6];
    	y[3]=m[1];
    	y[4]=m[4];
    	y[5]=m[7];
    	y[6]=m[2];
    	y[7]=m[5];
    	y[8]=m[8];
    	return y;
    }
    public float[] Usym (float[] x,float sym[]){
    	  /*! Applies the symmetry matrix sym to Uij's 
    	   * @param[in] x Uij matrix.
    	   * @param[in] sym symmtry matrix.
    	   * @param[out] y resulting Uij matrix. 
    	   */
    	float y[]=new float[9];float z[]=new float[9];
    	  //y=(transponse(sym)*x)*sym;
    	mmult(z,sym,x);
    	mmult(y,z,transponse(sym));
        return y;    
   	}
    
    float fl(float x, float y, float z){
  	  float aa,bb,cc;
  	  aa = (ga==90.0f)?0.0f:2.0f*x*y*a*b*cosga;
  	  bb = (be==90.0f)?0.0f:2.0f*x*z*a*c*cosbe;
  	  cc = (al==90.0f)?0.0f:2.0f*y*z*b*c*cosal;
  	  float erg=(float)Math.sqrt(
  			  x*x*a*a+
  			  y*y*b*b+
  			  z*z*c*c+
  			  aa+bb+cc);
  	  return erg;
  	}
    
    float ueq(float[] m){
    	float erg=0;
    	  erg+=m[0]*as*a*a*as;
    	  erg+=m[1]*as*a*b*bs;
    	  erg+=m[2]*as*a*c*cs;
    	  erg+=m[3]*bs*b*a*as;
    	  erg+=m[4]*bs*b*b*bs;
    	  erg+=m[5]*bs*b*c*cs;
    	  erg+=m[6]*cs*c*a*as;
    	  erg+=m[7]*cs*c*b*bs;
    	  erg+=m[8]*cs*c*c*cs;
    	  erg*=1/3.0;
    	return erg;
    }
    
    public void applyLatticeCentro(int gitter){
    	  /*! Adds centrings and inversion symmetry to the symmops and trans list. 
    	   * @param gitter the parameter of the shelx LATT instruction. ==> see Shelxl manual.
    	   */
    	  int z=symmops.size();
    	  float []inv={-1.0f,0.0f,0.0f, 0.0f,-1.0f,0.0f, 0.0f,0.0f,-1.0f};  
    	  centeric=false;
    	  if (gitter>0){ 
    	    for (int i=0; i<z;i++){
    	      float []m = new float[9];
    	      mmult(m,symmops.get(i),inv);
    	      symmops.add(m);
    	      trans.add(trans.get(i));
    	    }
    	  centeric=true;
    	  }
    	  gitter=(gitter>0)?gitter:-gitter;
    	  z=symmops.size();
    	  centered=false;
    	  switch (gitter){
    		  case 5 :
    			  for (int i=0; i<z;i++){
    			    Vector3D tt= new Vector3D();
    			    tt.add(trans.get(i));
    			    tt.add(0.0f, 0.5f, 0.5f);
    			    tt.x=(tt.x>1)?tt.x-1:tt.x;
    			    tt.y=(tt.y>1)?tt.y-1:tt.y;
    			    tt.z=(tt.z>1)?tt.z-1:tt.z;
    			    symmops.add(symmops.get(i));
    			    trans.add(tt);
    			    centered=true;
    			  }
    			  break;
    		  case 6 :
    			  for (int i=0; i<z;i++){
      			    Vector3D tt= new Vector3D();
      			    tt.add(trans.get(i));
      			    tt.add(0.5f, 0.0f, 0.5f);
    			    tt.x=(tt.x>1)?tt.x-1:tt.x;
    			    tt.y=(tt.y>1)?tt.y-1:tt.y;
    			    tt.z=(tt.z>1)?tt.z-1:tt.z;
    			    symmops.add(symmops.get(i));
    			    trans.add(tt);
    			    centered=true;
    			  }
    			  break;
    		  case 7 :
    			  for (int i=0; i<z;i++){
      			    Vector3D tt= new Vector3D(); 
      			    tt.add(trans.get(i));
      			    tt.add(0.5f, 0.5f, 0.0f);
    			    tt.x=(tt.x>1)?tt.x-1:tt.x;
    			    tt.y=(tt.y>1)?tt.y-1:tt.y;
    			    tt.z=(tt.z>1)?tt.z-1:tt.z;
    			    symmops.add(symmops.get(i));
    			    trans.add(tt);
    			    centered=true;
    			  }
    			  break;
    		  case 4 :
    			  for (int i=0; i<z;i++){
      			    Vector3D tt= new Vector3D(); 
      			    tt.add(trans.get(i));
      			    tt.add(0.0f, 0.5f, 0.5f);
    			    tt.x=(tt.x>1)?tt.x-1:tt.x;
    			    tt.y=(tt.y>1)?tt.y-1:tt.y;
    			    tt.z=(tt.z>1)?tt.z-1:tt.z;
    			    symmops.add(symmops.get(i));
    			    trans.add(tt);
    			    tt.zero(); 
    			    tt.add(trans.get(i));
    			    tt.add(0.5f, 0.0f, 0.5f);
    			    tt.x=(tt.x>1)?tt.x-1:tt.x;
    			    tt.y=(tt.y>1)?tt.y-1:tt.y;
    			    tt.z=(tt.z>1)?tt.z-1:tt.z;
    			    symmops.add(symmops.get(i));
    			    trans.add(tt);

    			    tt.zero(); 
    			    tt.add(trans.get(i));
    			    tt.add(0.5f, 0.5f, 0.0f);
    			    tt.x=(tt.x>1)?tt.x-1:tt.x;
    			    tt.y=(tt.y>1)?tt.y-1:tt.y;
    			    tt.z=(tt.z>1)?tt.z-1:tt.z;
    			    symmops.add(symmops.get(i));
    			    trans.add(tt);
    			    centered=true;
    			  }
    			  break;
    		  case 2 :
    			  for (int i=0; i<z;i++){
    			    Vector3D tt= new Vector3D();
    			    tt.add(trans.get(i));
    			    tt.add(0.5f, 0.5f, 0.5f);
    			    tt.x=(tt.x>1)?tt.x-1:tt.x;
    			    tt.y=(tt.y>1)?tt.y-1:tt.y;
    			    tt.z=(tt.z>1)?tt.z-1:tt.z;
    			    symmops.add(symmops.get(i));
    			    trans.add(tt);
    			    centered=true;
    			  }
    			  break;
    		  case 3 :
    			  for (int i=0; i<z;i++){
    			    Vector3D tt= new Vector3D();
    			    tt.add(trans.get(i));
    			    tt.add(2.0f/3.0f, 1.0f/3.0f, 1.0f/3.0f);
    			    tt.x=(tt.x>1)?tt.x-1:tt.x;
    			    tt.y=(tt.y>1)?tt.y-1:tt.y;
    			    tt.z=(tt.z>1)?tt.z-1:tt.z;
    			    symmops.add(symmops.get(i));
    			    trans.add(tt);
    			    tt.zero();
    			    tt.add(trans.get(i));
    			    tt.add(1.0f/3.0f, 2.0f/3.0f, 2.0f/3.0f);
    			    tt.x=(tt.x>1)?tt.x-1:tt.x;
    			    tt.y=(tt.y>1)?tt.y-1:tt.y;
    			    tt.z=(tt.z>1)?tt.z-1:tt.z;
    			    symmops.add(symmops.get(i));
    			    trans.add(tt);
    			    centered=true;

    			  }
    			  break;
    		  case 0 :break;  
    	  }

    	}

    public boolean decodeSymmCard(String symmCard){ 
    	 /*! decodes a symmetry card like 'SYMM -X, 1/2+Y, -Z' and feeds cell.symmops and cell.trans lists.
    	  * @param symmCard like 'SYMM -X, 1/2+Y, -Z'.
    	  * \returns true on sucess.
    	  * */ 
    	  String sc=symmCard.toUpperCase(Locale.US).replace("SYMM","").trim();
    	  sc=sc.replace("'","");
    	  sc=sc.replace(" ","");
    	  String axe[]=sc.split(",");
    	  String bruch[];
    	  if (axe.length!=3) return false;
    	  float sx[]={0,0,0},
    	  sy[]={0,0,0},
    	  sz[]={0,0,0},
    	  t[]={0,0,0};
    	  for (int i=0; i<3; i++){
    	    sx[i]=0;sy[i]=0;sz[i]=0;t[i]=0;
    	    if (axe[i].contains("-X")) {
    	    	sx[i]=-1.0f;
    	    	axe[i]=axe[i].replace("-X","");
    	    	}
    	    else if (axe[i].contains("X")) {
    	    	sx[i]=1.0f;
    	    	axe[i]=axe[i].replace("X","");
    	    	}
    	    if (axe[i].contains("-Y")) {sy[i]=-1.0f;axe[i]=axe[i].replace("-Y","");}
    	    else if (axe[i].contains("Y")) {sy[i]=1.0f;axe[i]=axe[i].replace("Y","");}
    	    if (axe[i].contains("-Z")) {sz[i]=-1.0f;axe[i]=axe[i].replace("-Z","");}
    	    else if (axe[i].contains("Z")) {sz[i]=1.0f;axe[i]=axe[i].replace("Z","");}
    	    if (axe[i].endsWith("+")) axe[i]=axe[i].replace("+","");

    	    if (axe[i].contains("/")) {
    	      bruch=axe[i].split("/");
    	      if (bruch.length==2) {
    	    	  t[i]=Float.valueOf(bruch[0]) / Float.valueOf(bruch[1]);
    	      }
    	    }
    	    else if (!axe[i].isEmpty()) t[i]=Float.valueOf(axe[i]);
    	  }
    	  float [] sm = matrix(sx[0],sy[0],sz[0],	  sx[1],sy[1],sz[1],  sx[2],sy[2],sz[2]);

    	  symmops.add(sm);
    	  trans.add(new Vector3D(t[0],t[1],t[2]));
    	  return true;
    	}
    
	static public float[] matrix(float m1, float m2, float m3, float m4,
			float m5, float m6, float m7, float m8, float m9) {
		float[] erg = new float[9];
		erg[0] = m1;
		erg[1] = m2;
		erg[2] = m3;
		erg[3] = m4;
		erg[4] = m5;
		erg[5] = m6;
		erg[6] = m7;
		erg[7] = m8;
		erg[8] = m9;
		return erg;
	}
    //QQuaternion jacobi(const QMatrix4x4 &uij, QVector3D &ev);
    public float jacobi(Vector3D ax, float[] uij, Vector3D ev) {
          float ang=0;
    	  int j,iq,ip,i,n=3;//nrot;
    	  float tresh=0,theta,tau,t,sm,s,h,g,c;
    	  float A[][],B[],z[],v[][],d[];
    	  A=new float[3][3];
    	  B=new float[3];
    	  v=new float[3][3];

    	  z=new float[3];
    	  d=new float[3];
    	  A[0][0]=uij[0];
    	  A[0][1]=uij[1];
    	  A[0][2]=uij[2];
    	  A[1][0]=uij[3];
    	  A[1][1]=uij[4];
    	  A[1][2]=uij[5];
    	  A[2][0]=uij[6];
    	  A[2][1]=uij[7];
    	  A[2][2]=uij[8];
    	  //QQuaternion erg;
    	  for (ip=1;ip<=n;ip++) {
    	    for (iq=1;iq<=n;iq++) v[ip-1][iq-1]=0.0f;
    	    v[ip-1][ip-1]=1.0f;
    	  }
    	  for (ip=1;ip<=n;ip++) {
    	    B[ip-1]=d[ip-1]=A[ip-1][ip-1];
    	    z[ip-1]=0.0f;
    	  }
    	  //nrot=0;
    	  for (i=1;i<=150;i++) {
    	    sm=0.0f;
    	    for (ip=1;ip<=n-1;ip++) {
    	      for (iq=ip+1;iq<=n;iq++)
    	    sm += Math.abs(A[ip-1][iq-1]);
    	    }

    	    //printf("sm =%20.19f\n",sm);

    	    if ((float)(sm) < tresh) {
    	      if ((v[0][0]+v[1][1]+v[2][2])!=3.0) {
    	          ang=(float)(Math.acos((v[0][0]+v[1][1]+v[2][2]-1.0)/2.0));
    	          ax.x=(float)((v[2][1]-v[1][2])/(2.0*Math.sin(ang)));
    	          ax.y=(float)((v[0][2]-v[2][0])/(2.0*Math.sin(ang)));
    	          ax.z=(float)((v[1][0]-v[0][1])/(2.0*Math.sin(ang)));
    	          ang=(float)(ang*180.0/Math.PI);
    	          ax=ax.normalized();;
    	      }
    	      else {
    	    	  ang=0f;
    	    	  ax.x=1;ax.y=0;ax.z=0;
    	    	  }
    	      //printf("%d??ERG:%f %f %f %f\n",i,Ato4d(erg));
    	      /*
    	      printf("=%d======================================\n%8.5f %8.5f %8.5f \n%8.5f %8.5f %8.5f \n%8.5f %8.5f %8.5f \n%8.5f %8.5f %8.5f\n========================================\n",i,
    	              d[0],d[1],d[2],v[0][0],v[0][1],v[0][2]
    	          ,v[1][0],v[1][1],v[1][2]
    	          ,v[2][0],v[2][1],v[2][2]
    	          );*/
    	     
    	    	ev.x=	 (float)(1.54*Math.sqrt(d[0]));
    	    	ev.y=	 (float)(1.54*Math.sqrt(d[1]));
    	    	ev.z=	 (float)(1.54*Math.sqrt(d[2]));;
    	     return ang;
    	    }
    	    if (i < 4) tresh=0.00001f;
    	    else tresh=0.0001f;
    	    for (ip=1;ip<=n-1;ip++) {
    	      for (iq=ip+1;iq<=n;iq++) {
    	    //printf("\np:%i q:%i i:%i nrot:%i\n",ip,iq,i,nrot);
    	    g=(float)(100.0*Math.abs(A[ip-1][iq-1]));
    	    if ((i > 4) && ((Math.abs(d[ip-1])+g) == Math.abs(d[ip-1])) && ((Math.abs(d[iq-1])+g) == Math.abs(d[iq-1]))) {A[ip-1][iq-1]=0.0f;}
    	    else if (Math.abs(A[ip-1][iq-1]) >= tresh) {
    	      h=d[iq-1]-d[ip-1];
    	      if ((Math.abs(h)+g) == Math.abs(h)) {t=(A[ip-1][iq-1])/h; }
    	      else { theta=0.5f*h/(A[ip-1][iq-1]);
    	      t=(float)(1.0/(Math.abs(theta)+Math.sqrt(1.0+theta*theta)));
    	      if (theta < 0.0) {t = -1.0f*t;}
    	      }
    	      c=(float)(1.0/Math.sqrt(1+t*t));
    	      s=t*c;
    	      tau=s/(1.0f+c);
    	      h=t*A[ip-1][iq-1];
    	      z[ip-1] -= h;
    	      z[iq-1] += h;
    	      d[ip-1] -= h;
    	      d[iq-1] += h;
    	      A[ip-1][iq-1]=0.0f;
    	      for (j=1;j<=ip-1;j++) {
    	    	  g=A[j-1][ip-1];h=A[j-1][iq-1];A[j-1][ip-1]=g-s*(h+g*tau); A[j-1][iq-1]=h+s*(g-h*tau);
    	          //printf("%i %i %i %i",j,ip,j,iq);
    	          }
    	      for (j=ip+1;j<=iq-1;j++) {
    	    	  g=A[ip-1][j-1];h=A[j-1][iq-1];A[ip-1][j-1]=g-s*(h+g*tau); A[j-1][iq-1]=h+s*(g-h*tau);
    	          //printf("%i %i %i %i ",ip,j,j,iq);
    	          }
    	      for (j=iq+1;j<=n;j++) {
    	    	  g=A[ip-1][j-1];h=A[iq-1][j-1];A[ip-1][j-1]=g-s*(h+g*tau); A[iq-1][j-1]=h+s*(g-h*tau);
    	          //printf("%i %i %i %i",ip,j,iq,j);
    	          }
    	      for (j=1;j<=n;j++) {
    	    	  g=v[j-1][ip-1];h=v[j-1][iq-1];v[j-1][ip-1]=g-s*(h+g*tau); v[j-1][iq-1]=h+s*(g-h*tau);
    	          }
    	    //  ++(nrot);
    	      //    printf("U|\n%f %f %f  \n%f %f %f\n%f %f %f\nV|\n%f %f %f  \n%f %f %f\n%f %f %f\n\n",a[0][0],a[1][0],a[2][0],a[0][1],a[1][1],a[2][1],a[0][2],a[1][2],a[2][2],v[0][0],v[1][0],v[2][0],v[0][1],v[1][1],v[2][1],v[0][2],v[1][2],v[2][2]);
    	    } //else ;//printf("nix:%f p%i q%i",fabs(a[ip-1][iq-1]),ip,iq);
    	      }
    	    }
    	    for (ip=1;ip<=n;ip++) {
    	      B[ip-1] += z[ip-1];
    	      d[ip-1] =B[ip-1];
    	      z[ip-1] =0.0f;
    	    }
    	  }
    	  ang=(float)(Math.acos((v[0][0]+v[1][1]+v[2][2]-1.0)/2.0));
    	  if (ang==0f) {
	    	  ax.x=1;ax.y=0;ax.z=0;
    	  }else{
    	  ax.x=(float)((v[2][1]-v[1][2])/(2.0*Math.sin(ang)));
    	  ax.y=(float)((v[0][2]-v[2][0])/(2.0*Math.sin(ang)));
    	  ax.z=(float)((v[1][0]-v[0][1])/(2.0*Math.sin(ang)));
    	  ang=(float)(ang*180.0/Math.PI);
    	  ax=ax.normalized();
    	  }
    	  /*printf("=%d=======================================\n%8.5f %8.5f %8.5f \n%8.5f %8.5f %8.5f \n%8.5f %8.5f %8.5f \n%8.5f %8.5f %8.5f\n========================================\n",i,
    	          d[0],d[1],d[2],v[0][0],v[0][1],v[0][2]
    	          ,v[1][0],v[1][1],v[1][2]
    	          ,v[2][0],v[2][1],v[2][2]

    	          );*/

	    	ev.x=	 (float)(1.54*Math.sqrt(d[0]));
	    	ev.y=	 (float)(1.54*Math.sqrt(d[1]));
	    	ev.z=	 (float)(1.54*Math.sqrt(d[2]));;
    	 return ang;
    	}
    //! the dimension a in Angstrom
    float a;
    //! the dimension b in Angstrom
    float b;
    //! the dimension c in Angstrom
    float c;
    //! the angle alpha in degrees
    float al;
    //! the angle beta in degrees
    float be;
    //! the angle gamma in degrees
    float ga;
    //! \f$\varphi =  \sqrt(1 - (\cos(\alpha)^2) - (\cos(\beta)^2) - (\cos(\gamma)^2) + 2\cos(\alpha)\cos(\beta)\cos(\gamma))\f$
    float phi;
    //! the cell volume in Angstrom^3
    float V;
    //! the reciprocal dimension a
    float as;
    //! the reciprocal dimension b
    float bs;
    //! the reciprocal dimension c
    float cs;
    //! \f$ \tau = c ((\cos(\alpha) - \cos(\beta)  \cos(\gamma)) / \sin(\gamma))\f$
    float tau;
    //! \f$ \cos(\gamma)\f$
    float cosga;
    //! \f$ \cos(\alpha) \f$
    float cosal;
    //! \f$ \cos(\beta) \f$
    float cosbe;
    //! \f$ \sin(\gamma) \f$
    float singa;
    //! \f$ \tan(\gamma) \f$
    float tanga;
    //! List of symmetry operators
    ArrayList<float[]> symmops=new ArrayList<float[]>();
    // //! List of translations
    ArrayList<Vector3D> trans=new ArrayList<Vector3D>();
    //! space group is centred (A,B,C,I,F)
    boolean centered;
     //! space group is centro symmetric
    boolean centeric;
    //! ZERR string as it is from the res file
    String Z;
    //! the wavelenth  \f$ \lambda  \f$ in Angstroms
    double wave;
};