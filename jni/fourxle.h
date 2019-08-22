
/****************************************************************************
**
** Copyright (C) 2011 Christian B. Huebschle & George M. Sheldrick
** All rights reserved.
** Contact: chuebsch@moliso.de
**
** This file is part of the ShelXle
**
** This file may be used under the terms of the GNU Lesser
** General Public License version 2.1 as published by the Free Software
** Foundation and appearing in the file COPYING included in the
** packaging of this file.  Please review the following information to
** ensure the GNU Lesser General Public License version 2.1 requirements
** will be met: http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html.
**
**
****************************************************************************/
#ifndef FOURXLE_H
#define FOURXLE_H 1
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <ctype.h>
#include <string.h>
//#include <fftw3.h> //if you want you can try to uncoment this, but then you have to uncoment the following line
#include "kissfft/kiss_fftnd.h"
//#include "molecule.h"
//#include "chgl.h"
//! V3 is a three dimensional vector in cartesian space
struct V3 {
  float x//! x is the X coordinate
	 ,y//! y is the Y coordinate
	 ,z//! z is the Z coordinate
	 ;
//  int rc;
  inline V3( void ){}
  inline V3( const float& _x, const float& _y, const float& _z ) :
  x(_x), y(_y), z(_z)//!< initializer
       	//,rc(0)
  {
    ;
  }
  inline V3& operator *= ( const float& d ){
    x *= d;
    y *= d;
    z *= d;
    return *this;
  }//!< The *= operator to scale by a scalar
  inline V3& operator += ( const V3& v ){
    x += v.x;
    y += v.y;
    z += v.z;
    return *this;
  }//!< The += operator to add a V3
  inline V3& operator += ( const float& v ){
    x += v;
    y += v;
    z += v;
    return *this;
  }//!< The += operator to add a scalar
};
inline V3 operator + ( const V3& v1, const V3& v2 ) {
  V3 t;
  t.x = v1.x + v2.x;
  t.y = v1.y + v2.y;
  t.z = v1.z + v2.z;
  return t;
}//!< The + operator to add two V3
inline V3 operator - ( const V3& v1, const V3& v2 ) {
  V3 t;
  t.x = v1.x - v2.x;
  t.y = v1.y - v2.y;
  t.z = v1.z - v2.z;
  return t;
}//!< The + operator to subtract two V3
inline V3 operator * ( const V3& v, const float& d ) {
  V3 t;
  t.x = v.x*d;
  t.y = v.y*d;
  t.z = v.z*d;
  return t;
}//!< The * to scale a V3
inline V3 operator * ( const float& d, const V3& v ) {
  V3 t;
  t.x = v.x*d;
  t.y = v.y*d;
  t.z = v.z*d;
  return t;
}//!< The * to scale a V3
inline V3 operator % ( const V3& v1, const V3& v2 ) {
  V3 t;
  t.x = v1.y*v2.z - v2.y*v1.z;
  t.y = v1.z*v2.x - v2.z*v1.x;
  t.z = v1.x*v2.y - v2.x*v1.y;
  return t;
}//!< The % operator the cross product of two V3
inline float operator * ( const V3& v1, const V3& v2 ) {
  return v1.x*v2.x + v1.y*v2.y + v1.z*v2.z;
}//!< The * operator the scalar product of two V3
inline float Norm( const V3& v ) {
  return v.x*v.x + v.y*v.y + v.z*v.z;
}//!< The squared lenght of a V3
inline float Distance( const V3& v1, const V3& v2 ) {
  return Norm(v1 - v2);
}//!< The squared distance between two V3
inline bool operator == (const V3& v1, const V3& v2 ) {
  //  return ((v1.x==v2.x)&&(v1.y==v2.y)&&(v1.z==v2.z));
  return (Distance(v1,v2)<0.001);
}
inline V3& Normalize( V3 v ) {
static V3 erg=V3(1,0,0);
  if (Norm(v))  erg= (v * (1.0/sqrt(Norm(v))));
  return erg;
}
//! Matrix is a 3 x 3 Matrix with all needed operators
struct Matrix{
float m11, m21, m31, m12, m22, m32, m13, m23, m33;
 inline Matrix(void){}
 inline Matrix( const V3 &a, const V3 &b, const V3 &c):
	 m11(a.x), m21(b.x), m31(c.x),
	 m12(a.y), m22(b.y), m32(c.y),
	 m13(a.z), m23(b.z), m33(c.z){;}
 inline Matrix( const float& x11, const float& x21, const float& x31,
                const float& x12, const float& x22, const float& x32,
                const float& x13, const float& x23, const float& x33):
	 m11(x11), m21(x21), m31(x31),
	 m12(x12), m22(x22), m32(x32),
	 m13(x13), m23(x23), m33(x33){;}

};
 inline Matrix transponse (Matrix a){//transponse
    return Matrix(
		  a.m11, a.m12, a.m13,
		  a.m21, a.m22, a.m23,
		  a.m31, a.m32, a.m33);
 }
 inline bool operator == (const Matrix &a,const Matrix &b){
     return ((a.m11 == b.m11)&&(a.m21 == b.m21)&&(a.m31 == b.m31)&&
     (a.m12 == b.m12)&&(a.m22 == b.m22)&&(a.m23 == b.m23)&&
     (a.m13 == b.m13)&&(a.m32 == b.m32)&&(a.m33 == b.m33));
 }
inline Matrix operator * (const Matrix &a,const Matrix &b){
  Matrix erg;
  erg.m11 = a.m11 * b.m11 + a.m21 * b.m12 + a.m31 * b.m13;
  erg.m21 = a.m11 * b.m21 + a.m21 * b.m22 + a.m31 * b.m23;
  erg.m31 = a.m11 * b.m31 + a.m21 * b.m32 + a.m31 * b.m33;

  erg.m12 = a.m12 * b.m11 + a.m22 * b.m12 + a.m32 * b.m13;
  erg.m22 = a.m12 * b.m21 + a.m22 * b.m22 + a.m32 * b.m23;
  erg.m32 = a.m12 * b.m31 + a.m22 * b.m32 + a.m32 * b.m33;

  erg.m13 = a.m13 * b.m11 + a.m23 * b.m12 + a.m33 * b.m13;
  erg.m23 = a.m13 * b.m21 + a.m23 * b.m22 + a.m33 * b.m23;
  erg.m33 = a.m13 * b.m31 + a.m23 * b.m32 + a.m33 * b.m33;
  return erg;
}
inline V3 operator * (const Matrix &a, const V3 &b){
  V3 erg;
  erg.x = a.m11*b.x + a.m21*b.y + a.m31*b.z;
  erg.y = a.m12*b.x + a.m22*b.y + a.m32*b.z;
  erg.z = a.m13*b.x + a.m23*b.y + a.m33*b.z;
  return erg;
}
inline V3 operator * (const V3 &a, const Matrix &b){
  V3 erg;
  erg.x = b.m11*a.x + b.m12*a.y + b.m13*a.z;
  erg.y = b.m21*a.x + b.m22*a.y + b.m23*a.z;
  erg.z = b.m31*a.x + b.m32*a.y + b.m33*a.z;
  return erg;
}
//#include <QObject>
//#include <QCheckBox>
//! Rec is a reflection type of a fcf 6 file.
typedef struct {
int  ih,//!< h
     ik,//!< k
     il;//!< l 
float fo,//!< F observed
      so,//!< \f$\sigma(observed)\f$
      fc,//!< F calculated
      phi; //!< \f$\varphi\f$ 
} Rec;
#define LM 2000000
#define qMin(a,b) (a>b)?b:a
//! FNode is a struct for the 3 different edges of a cube 
struct FNode {
  V3 vertex;//!< vertex is the 3d position of the node
//  V3 normal;//!< normal is the plane normal of the surface
  char flag;//!< this flag is set when the surface crosses this node
  inline operator char (){
    //! \returns the flag parameter. No Coala ;-)
    return flag;
  }
  inline operator V3(){
    //! \returns the vertex of the node.
    return vertex;
  }
  friend inline float Distance( const FNode& n1, const FNode& n2 ){
    /*! @param n1,n2 nodes vor which the squared distance should be calculated.
     \returns te positional squared distance between two nodes
     */
    return Norm(n1.vertex-n2.vertex);
  }
};
//
//!  FourXle was an QObject that loads a fcf 6 file and creates iso-surfaces in meshed style \inherit QObject
class FourXle{
  public:
	int nFoPl,nFoMi,nDiPl,nDiMi,nPos;
	float *foPlus,*foMinus,*diMinus,*diPlus,*posi,*fraci;
          int HKLMX;//!< Maximum of h,k and l values can be used to make a resulution cut. 
          float *datfo,//!<data pointer for the fobs map in real space
		*datfo_fc;//!<data pointer for the fobs-fcalc map in real space
          FourXle();
	  /*!< @param resol resolution factor (the higher the more grid points)
	   *  @param wght a parameter to downweight weak reflections
	   */
          ~FourXle();
          bool loadFouAndPerform(const char filename[],bool neu=true);
          float sigma[3];//!<sigma values 
          float iso[3];//!<iso values
	//  float mInimum[2],mAximum[2];
	      //V3 urs;//!< origin
          float rr,//!< resolution factor (the higher the more grid points)
		 rw;//!< a parameter to downweight weak reflections

/*	  QColor fopc,//!< Fo positive color
		 fomc,//!< Fo negative color
		 dipc,//!< Fo-Fc positive color
		 dimc;//!< Fo-Fc negative color */
	  void deleteLists();
	  void killmaps();
	  int n1,//!< dimension of the map in a diRection
	      n2,//!< dimension of the map in b diRection
	      n3,//!< dimension of the map in c diRection
	      n4;//!< \f$ n4 = n1 \times n2\f$
	  int n5;//!< \f$ n5 = n1 \times n2 \times n3\f$
	  V3 dx,//!< vector in a direction for each map voxel
	     dy,//!< vector in b direction for each map voxel
	     dz;//!< vector in c direction for each map voxel
  //public slots:
   //       void bewegt(V3 v);
  //        void inimap();
          void change_iso(int numsteps,int diff);

  private:

          V3 frac2cart(V3 x);
          V3 cart2frac(V3 x);
	  float C[15],D[9],sy[12][192],wave;
	  inline int Intersect( float& vm, float& vp ){ return vm*vp <= 0.0f && (vm<0.0f || vp<0.0f); }
      inline int dex(int x,int y,int z);

      inline int dex3(int x,int y,int z);
	  int oldatomsize;
	  int acnt,nobs,nobsm,ndifm,ndifp;
	  V3 oc,dxc,dyc,dzc;
	  V3  delDA[27];
	  kiss_fftnd_cfg fwd_plan;//!!!
          kiss_fft_cpx *B;//!!!
          FNode *nodex,*nodey,*nodez;
	  int *noGoMap;
	  Rec lr[LM];
	  char cen,git;
          int nr,nc,ns;
	  int mtyp,tri;
          void gen_surface(bool neu,int imin=0, int imax=3);
          void CalcVertex( int ix, int iy, int iz);
	  V3 CalcNormalX( int ix, int iy, int iz );
	  V3 CalcNormalY( int ix, int iy, int iz );
	  V3 CalcNormalZ( int ix, int iy, int iz );
          int IndexSelected( FNode& node0, FNode& node1, FNode& node2, FNode& node3 );
          V3& VectorSelected( FNode& node0, FNode& node1, FNode& node2, FNode& node3 );
          void MakeElement( int ix, int iy, int iz ,int s1, int s2,int fac);
          void makeFaces(int n, FNode poly[] ,int fac);

char titl[80];/*fcmax=0,f000=0,resmax=99999.0,*/
void trimm(char s[]);
void deletes(char *s, int count);
int readHeader(const char *filename);
void sorthkl(int nr, Rec r[]);
};

#endif
