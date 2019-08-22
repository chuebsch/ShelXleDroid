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
#include "fourxle.h"
#include <jni.h>
#include "fourxle_x.h"
#include <android/log.h>
#define LOG_TAG "FourXle"
#define LOGI(...) do { __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__); } while(0)
#define LOGW(...) do { __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__); } while(0)
#define LOGE(...) do { __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__); } while(0)

FourXle *fxle=NULL;
JNIEXPORT jfloatArray JNICALL Java_de_moliso_shelxle_FourXle_loadFouAndPerform(
		JNIEnv *env, jobject thisObj, jstring s1, jboolean neu,
		 jfloatArray pos) {

	fxle = new FourXle();
	LOGI("before fft 123\n");
	//LOGI(env->GetStringUTFChars(s1, NULL));

	fxle->fraci=env->GetFloatArrayElements(pos,NULL);
	LOGI("org: %g %g %g",fxle->fraci[0],fxle->fraci[1],fxle->fraci[2]);
	fxle->nPos=env->GetArrayLength(pos);
	bool b=fxle->loadFouAndPerform(env->GetStringUTFChars(s1, NULL), (bool) neu);
	LOGI("After nFo+ %d nDi- %d nDi+ %d done? %d\n",fxle->nFoPl,fxle->nDiMi,fxle->nDiPl,b);
	/*dimi = env->NewFloatArray(fxle->nDiMi);
	env->SetFloatArrayRegion(dimi, 0, fxle->nDiMi, fxle->diMinus);
	dipl = env->NewFloatArray(fxle->nDiPl);*/
	//env->SetFloatArrayRegion(dipl, 0, fxle->nDiPl, fxle->diPlus);
/*
	fxle->foPlus = (float*) realloc(fxle->foPlus,  sizeof(float) * fxle->nFoPl);
	fxle->foMinus = (float*) realloc(fxle->foMinus,sizeof(float) * fxle->nFoMi);
	fxle->diMinus = (float*) realloc(fxle->diMinus,sizeof(float) * fxle->nDiPl);
	fxle->diPlus = (float*) realloc(fxle->diPlus,  sizeof(float) * fxle->nDiMi);*/
	jfloatArray fopl = env->NewFloatArray(fxle->nFoPl);
	env->SetFloatArrayRegion(fopl, 0, fxle->nFoPl, fxle->foPlus);

	LOGI("loadAndPerform is finished!");
	return fopl;
}

/*
 * Class:     de_moliso_shelxle_FourXle
 * Method:    getFoPlus
 * Signature: ()[F
 */
JNIEXPORT jfloatArray JNICALL Java_de_moliso_shelxle_FourXle_getFoPlus
  (JNIEnv *env, jobject thisObj){
	if (fxle==NULL) return NULL;
	jfloatArray fopl = env->NewFloatArray(fxle->nFoPl);
	env->SetFloatArrayRegion(fopl, 0, fxle->nFoPl, fxle->foPlus);
	return fopl;
}

/*
 * Class:     de_moliso_shelxle_FourXle
 * Method:    getFoMinus
 * Signature: ()[F
 */
JNIEXPORT jfloatArray JNICALL Java_de_moliso_shelxle_FourXle_getFoMinus
  (JNIEnv *env, jobject thisObj){
	if (fxle==NULL) return NULL;
	jfloatArray fomi = env->NewFloatArray(fxle->nFoMi);
	env->SetFloatArrayRegion(fomi, 0, fxle->nFoMi, fxle->foMinus);
	return fomi;

}

/*
 * Class:     de_moliso_shelxle_FourXle
 * Method:    getDifPlus
 * Signature: ()[F
 */
JNIEXPORT jfloatArray JNICALL Java_de_moliso_shelxle_FourXle_getDifPlus
  (JNIEnv *env, jobject thisObj){
	if (fxle==NULL) return NULL;
	jfloatArray dipl = env->NewFloatArray(fxle->nDiPl);
	env->SetFloatArrayRegion(dipl, 0, fxle->nDiPl, fxle->diPlus);
	return dipl;

}

/*
 * Class:     de_moliso_shelxle_FourXle
 * Method:    getDifMinus
 * Signature: ()[F
 */
JNIEXPORT jfloatArray JNICALL Java_de_moliso_shelxle_FourXle_getDifMinus
  (JNIEnv *env, jobject thisObj){
	if (fxle==NULL) return NULL;
	jfloatArray dimi = env->NewFloatArray(fxle->nDiMi);
	env->SetFloatArrayRegion(dimi, 0, fxle->nDiMi, fxle->diMinus);
	return dimi;
}



JNIEXPORT void JNICALL Java_de_moliso_shelxle_FourXle_change_1iso
(JNIEnv *env, jobject thisObj, jint numsteps , jint diff, jfloatArray fopl, jfloatArray dimi, jfloatArray dipl) {
	fxle->change_iso(numsteps,diff);
	fopl=env->NewFloatArray(fxle->nFoPl);
	env->SetFloatArrayRegion(fopl,0,fxle->nFoPl,fxle->foPlus);
	dimi=env->NewFloatArray(fxle->nDiMi);
	env->SetFloatArrayRegion(dimi,0,fxle->nDiMi,fxle->diMinus);
	dipl=env->NewFloatArray(fxle->nDiPl);
	env->SetFloatArrayRegion(dipl,0,fxle->nDiPl,fxle->diPlus);
}

FourXle::FourXle() {
	nobs=3;
	nobsm=1;
	ndifm=1;
	ndifp=3;
	foPlus = (float*) malloc(sizeof(float) * nobs * 100000);
	foMinus = (float*) malloc(sizeof(float) * nobsm * 100000);
	diMinus = (float*) malloc(sizeof(float) * ndifm * 100000);
	diPlus = (float*) malloc(sizeof(float) * ndifp * 100000);
	nFoPl = 0;
	nDiMi = 0;
	nDiPl = 0;
	n1 = n2 = n3 = n4 = n5 = 0;
	oldatomsize = -1;
	/*chgl->foubas[0] = 0;
	 chgl->foubas[1] = 0;
	 chgl->foubas[2] = 0;*/
	HKLMX = 50;
	datfo_fc = datfo = NULL;
	nodex = nodey = nodez = NULL;
	noGoMap = NULL;
	//urs = V3(0, 0, 0);
	nr = 0;
	nc = 0;
	sigma[0] = sigma[1] = iso[0] = iso[1] = 0;
	//lintrans = 0.8;
	//linwidth = 0.5;
	rr = 2.0;
	rw = 1.0;
}

FourXle::~FourXle() {
	killmaps();
	//delete doMaps;
}

void FourXle::killmaps() {
	/*! deletes all fourier maps and frees the memory
	 */
	if (datfo != NULL)
		free(datfo);
	if (datfo_fc != NULL)
		free(datfo_fc);
	datfo = datfo_fc = NULL;
	deleteLists();
	if (nodex != NULL)
		free(nodex);
	if (nodey != NULL)
		free(nodey);
	if (nodez != NULL)
		free(nodez);
	if (noGoMap != NULL)
		free(noGoMap);
	nodex = nodey = nodez = NULL;
	noGoMap = NULL;

}

bool FourXle::loadFouAndPerform(const char filename[], bool neu) {
	/*! loads a fcf file prepares the reciprocal data for the fourier transpormation and performs it.
	 */

	const int it[143] = { 2, 3, 4, 5, 6, 8, 9, 10, 12, 15, 16, 18, 20, 24, 25,
			27, 30, 32, 36, 40, 45, 48, 50, 54, 60, 64, 72, 75, 80, 81, 90, 96,
			100, 108, 120, 125, 128, 135, 144, 150, 160, 162, 180, 192, 200,
			216, 225, 240, 243, 250, 256, 270, 288, 300, 320, 324, 360, 375,
			384, 400, 405, 432, 450, 480, 486, 500, 512, 540, 576, 600, 625,
			640, 648, 675, 720, 729, 750, 768, 800, 810, 864, 900, 960, 972,
			1000, 1024, 1080, 1125, 1152, 1200, 1215, 1250, 1280, 1296, 1350,
			1440, 1458, 1500, 1536, 1600, 1620, 1728, 1800, 1875, 1920, 1944,
			2000, 2025, 2048, 2160, 2187, 2250, 2304, 2400, 2430, 2500, 2560,
			2592, 2700, 2880, 2916, 3000, 3072, 3125, 3200, 3240, 3375, 3456,
			3600, 3645, 3750, 3840, 3888, 4000, 4050, 4096, 4320, 4374, 4500,
			4608, 4800, 4860, 5000 }; //!multiples of 2 3 and 5

	killmaps();
	int ok;
	//if (!doMaps->isChecked())
	//return false;
	if (strstr(filename, ".fcf") == NULL)
		return false;
	FILE *f;
	ok = readHeader(filename);
    if (ok!=0) return false;
	LOGI("cell %g %g %g %g %g %g %g pi=%g\n",C[0],C[1],C[2],C[3],C[4],C[5],C[14],M_PI);
	dx = cart2frac(V3(0.45,0,0));
	dy = cart2frac(V3(0,0.45,0));
	dz = cart2frac(V3(0,0,0.45));
	float ddxmax=fmax(1.0/dx.x,fmax(1.0/dy.y,1.0/dz.z));
	/*V3 vector=V3(0.25,0.25,0.25);
	LOGI("vector frac1 %g %g %g => %g",vector.x,vector.y,vector.z,ddxmax);
	vector=frac2cart(vector);

	LOGI("vector cart1 %g %g %g",vector.x,vector.y,vector.z);
	vector=cart2frac(vector);

	LOGI("vector frac2 %g %g %g",vector.x,vector.y,vector.z);*/
	f = fopen(filename, "rb");
	if (f == NULL)
		return false;
	char line[122] = "", *dum;
	while (strstr(line, "_refln_phase_calc") == NULL) {
		dum = fgets(line, 120, f);
	}
	nr = 0;
float hklMx=0;
	//emit bigmessage(QString::fromLocal8Bit(filename));
	while (!feof(f)) {
		dum = fgets(line, 120, f);
		int rdi = sscanf(line, "%d %d %d %f %f %f %f", &lr[nr].ih, &lr[nr].ik,
				&lr[nr].il, &lr[nr].fo, &lr[nr].so, &lr[nr].fc, &lr[nr].phi);
		hklMx=fmax(hklMx,fmax(abs(lr[nr].ih),fmax(abs(lr[nr].ik),abs(lr[nr].il))));
		if (rdi == 7)
			if ((abs(lr[nr].ih) < HKLMX) && (abs(lr[nr].ik) < HKLMX)
					&& (abs(lr[nr].il) < HKLMX)
					&& ((lr[nr].ih | lr[nr].ik | lr[nr].il) != 0))
				nr++;
		if (nr >= LM) {
			return false;
		}
	}
	fclose(f);
	if (!nr) {
		return false;
	}
    LOGI("rr %g %g %g %g",rr,hklMx,ddxmax,ddxmax/hklMx);
    rr=fmax(ddxmax/hklMx,1.4);
    LOGI("rr %g %g %g %g",rr,hklMx,ddxmax,ddxmax/hklMx);
	for (int i = 0; i < nr; i++) {
		float u = lr[i].ih, v = lr[i].ik, w = lr[i].il;
		int mh = lr[i].ih, mk = lr[i].ik, ml = lr[i].il;
		float p, q = lr[i].phi / 180.0 * M_PI;
		lr[i].phi = fmod(4.0f * M_PI + q, 2.0f * M_PI);
		for (int k = 0; k < ns; k++) {
			int nh, nk, nl;
			float t = 1.0;
			nh = (int) (u * sy[0][k] + v * sy[3][k] + w * sy[6][k]);
			nk = (int) (u * sy[1][k] + v * sy[4][k] + w * sy[7][k]);
			nl = (int) (u * sy[2][k] + v * sy[5][k] + w * sy[8][k]);
			if ((nl < 0) || ((nl == 0) && (nk < 0))
					|| ((nl == 0) && (nk == 0) && (nh < 0))) {
				nh *= -1;
				nk *= -1;
				nl *= -1;
				t = -1.0;
			}
			if ((nl < ml) || ((nl == ml) && (nk < mk))
					|| ((nl == ml) && (nk == mk) && (nh <= mh)))
				continue;
			mh = nh;
			mk = nk;
			ml = nl;
			p = u * sy[9][k] + v * sy[10][k] + w * sy[11][k];
			lr[i].phi = fmod(
					4 * M_PI + t * fmod(q - 2 * M_PI * p, 2 * M_PI) - 0.01,
					2 * M_PI) + 0.01;

		}
		lr[i].ih = mh;
		lr[i].ik = mk;
		lr[i].il = ml;
	}
	sorthkl(nr, lr);
	int n = -1;
	{
		int i = 0;
		while (i < nr) {
			float t = 0.;
			float u = 0.;
			float v = 0.;
			float z = 0.;
			float p = 0.;
			int m;
			int k = i;
			while ((i < nr) && (lr[i].ih == lr[k].ih) && (lr[i].ik == lr[k].ik)
					&& (lr[i].il == lr[k].il)) {
				t = t + 1.;
				u += lr[i].fo;
				v += 1. / (lr[i].so * lr[i].so);
				z += lr[i].fc;
				p = lr[i].phi;
				i++;
			}
			m = n + 1;
			lr[m].fo = sqrt(fmax(0., u / t));
			lr[m].so = sqrt(lr[m].fo * lr[m].fo + sqrt(1. / v)) - lr[m].fo;
			lr[m].fc = z / t;
			lr[m].phi = p;
			n = m;
			lr[n].ih = lr[k].ih;
			lr[n].ik = lr[k].ik;
			lr[n].il = lr[k].il;
		}
	}
	n++;
	nr = n;
	{
		float DX;
		float DY;
		float DZ;

		{
			int mh = 0, mk = 0, ml = 0, j;
			for (int n = 0; n < nr; n++) {
				float u = lr[n].ih, v = lr[n].ik, w = lr[n].il;
				float a, b, c;
				for (int k = 0; k < ns; k++) {
					a = abs((int) (u * sy[0][k] + v * sy[3][k] + w * sy[6][k]));
					b = abs((int) (u * sy[1][k] + v * sy[4][k] + w * sy[7][k]));
					c = abs((int) (u * sy[2][k] + v * sy[5][k] + w * sy[8][k]));
					mh = (mh < a) ? a : mh;
					mk = (mk < b) ? b : mk;
					ml = (ml < c) ? c : ml;
				}
			}
			j = (int) (rr * mh + .5);
			for (int i = 0; it[i] < j; i++)
				n1 = it[i + 1];
			j = (int) (rr * mk + .5);
			for (int i = 0; it[i] < j; i++)
				n2 = it[i + 1];
			j = (int) (rr * ml + .5);
			for (int i = 0; (it[i] < j) || ((nc) && (it[i] % 2)); i++)
				n3 = it[i + 1];
			n4 = n2 * n1;
			n5 = n3 * n4;
			datfo = (float*) malloc(sizeof(float) * n5);
			datfo_fc = (float*) malloc(sizeof(float) * n5);
			DX = 1.0 / n1;
			DY = 1.0 / n2;
			DZ = 1.0 / n3;
			LOGI("DX %g DY %g DZ %g\n",DX,DY,DZ);
		}
		for (int typ = 0; typ < 2; typ++) {
			float miZ = 99999.99, maZ = -99999.99;
//      mInimum[typ]=99999.99; mAximum[typ]=-99999.99;
			//B=(fftwf_complex*)fftwf_malloc(sizeof(fftwf_complex)*n5);
			int nbytes, dims[3];
			dims[0] = n3;
			dims[1] = n2;
			dims[2] = n1;
			B = (kiss_fft_cpx*) KISS_FFT_MALLOC(
					nbytes = (sizeof(kiss_fft_cpx) * n5));
			for (int i = 0; i < n5; i++) {
				B[i].r = 0;
				B[i].i = 0;
			}
			for (int i = 0; i < nr; i++) {
				float u, v, w;
				u = lr[i].ih;
				v = lr[i].ik;
				w = lr[i].il;
				float ss, s = 0, t = 0, q, p;
				for (int n = 0; n < ns; n++) {
					int j, k, l;
					j = (int) (u * sy[0][n] + v * sy[3][n] + w * sy[6][n]);
					k = (int) (u * sy[1][n] + v * sy[4][n] + w * sy[7][n]);
					l = (int) (u * sy[2][n] + v * sy[5][n] + w * sy[8][n]);
					if ((abs(j - lr[i].ih) + abs(k - lr[i].ik)
							+ abs(l - lr[i].il)) == 0)
						s += 1.0;
					if (abs(j + lr[i].ih) + abs(k + lr[i].ik)
							+ abs(l + lr[i].il) == 0)
						t += 1.0;
				}
				if (typ == 0)
					ss = (lr[i].fo - lr[i].fc) / (C[14] * (s + t));
				else if (typ == 2)
					ss = (lr[i].fc) / (C[14] * (s + t));
				else
					ss = (lr[i].fo) / (C[14] * (s + t));
				if (lr[i].fc > 1.E-6)
					ss = ss / (1. + rw * pow(lr[i].so / lr[i].fc, 4));
				for (int n = 0; n < ns; n++) {
					int j, k, l, m;
					j = (int) (u * sy[0][n] + v * sy[3][n] + w * sy[6][n]);
					k = (int) (u * sy[1][n] + v * sy[4][n] + w * sy[7][n]);
					l = (int) (u * sy[2][n] + v * sy[5][n] + w * sy[8][n]);
//          q=(-2*M_PI*(u*sy[9][n]+v*sy[10][n]+w*sy[11][n]))-M_PI*(j*DX+k*DY+l*DZ);
					q = (lr[i].phi
							- 2 * M_PI
									* (
											u * sy[9][n] +
											v * sy[10][n]+
											w * sy[11][n]))
							- M_PI * (j * DX + k * DY + l * DZ);
					j = (999 * n1 + j) % n1;
					k = (999 * n2 + k) % n2;
					l = (999 * n3 + l) % n3;
					m = j + n1 * (k + n2 * l);
					p = ss * cosf(q);
					B[m].r = p;
					q = ss * sinf(q);
					B[m].i = q;
					j *= -1;
					if (j < 0)
						j = n1 + j;
					k *= -1;
					if (k < 0)
						k = n2 + k;
					l *= -1;
					if (l < 0)
						l = n3 + l;
					m = j + n1 * (k + n2 * l);
					B[m].r = p;
					B[m].i = -q;
				}
			}
			fwd_plan = kiss_fftnd_alloc(dims, 3, 0, 0, 0);
//      fwd_plan = fftwf_plan_dft_3d(n3,n2,n1,B,B,FFTW_FORWARD,FFTW_ESTIMATE);
//      fftwf_execute(fwd_plan);
			kiss_fftnd(fwd_plan, B, B);
//      fftwf_destroy_plan(fwd_plan);
			free(fwd_plan);
			float t = 0;
			float DM = 0., DS = 0., DD;
			for (int i = 0; i < n5; i++) {
				DD = B[i].r;
//	maxi[typ]=fmax(maxi[typ],DD); mini[typ]=fmin(mini[typ],DD);
				miZ = fmin(miZ, DD);
				maZ = fmax(maZ, DD);
				DM += DD;
				DS += DD * DD;
				if (typ == 1)
					datfo[i] = B[i].r;
				else if (typ == 0)
					datfo_fc[i] = B[i].r;

			}
			sigma[typ] = t = sqrt((DS / n5) - ((DM / n5) * (DM / n5)));
			/*if (typ==1){
			      double r=9999.0/(maZ-miZ);
			      double s=1/r;
			      FILE *map=fopen("/storage/sdcard0/fxle_test.map","wb");
			      fprintf(map,"%5d%5d%5d%5d%12.8f%12.6f%12.6f\n",1,n1,n2,n3,s,miZ,t);
			      for (int mzi=0; mzi<n5; mzi++){
			          fprintf(map,"%4d ",(int)( (B[mzi].r-miZ)*r));
			          if (mzi%16==15)fprintf(map,"\n");
			      }
			      fclose(map);
			      }*/
			free(B);
		}      //1
	}      //2
	nodex = (FNode*) malloc(sizeof(FNode) * n5);
	nodey = (FNode*) malloc(sizeof(FNode) * n5);
	nodez = (FNode*) malloc(sizeof(FNode) * n5);
	noGoMap = (int*) malloc(sizeof(int) * n5 * 27);
	for (int no = 0; no < (n5 * 27); no++)
		noGoMap[no] = 0;
	for (int o = 0; o < n5; o++) {
		nodex[o].flag = 0;
		nodey[o].flag = 0;
		nodez[o].flag = 0;
	}
	dx = V3(1.0 / (n1), 0, 0);
	dy = V3(0, 1.0 / (n2), 0);
	dz = V3(0, 0, 1.0 / (n3));
	dx = frac2cart(dx);
	dy = frac2cart(dy);
	dz = frac2cart(dz);

	LOGI("dx.x %g dy.y %g dz.z %g\n",dx.x,dy.y,dz.z);

	delDA[0] = -n1 * dx - n2 * dy - n3 * dz;      //nx ny,nz??
	delDA[1] = -n2 * dy - n3 * dz;
	delDA[2] = n1 * dx - n2 * dy - n3 * dz;
	delDA[3] = -n1 * dx - n3 * dz;
	delDA[4] = -n3 * dz;
	delDA[5] = n1 * dx - n3 * dz;
	delDA[6] = -n1 * dx + n2 * dy - n3 * dz;
	delDA[7] = n2 * dy - n3 * dz;
	delDA[8] = n1 * dx + n2 * dy - n3 * dz;
	delDA[9] = -n1 * dx - n2 * dy;
	delDA[10] = -n2 * dy;
	delDA[11] = n1 * dx - n2 * dy;
	delDA[12] = -n1 * dx;
	delDA[13] = V3(0, 0, 0);
	delDA[14] = n1 * dx;
	delDA[15] = -n1 * dx + n2 * dy;
	delDA[16] = n2 * dy;
	delDA[17] = n1 * dx + n2 * dy;
	delDA[18] = -n1 * dx - n2 * dy + n3 * dz;
	delDA[19] = -n2 * dy + n3 * dz;
	delDA[20] = n1 * dx - n2 * dy + n3 * dz;
	delDA[21] = -n1 * dx + n3 * dz;
	delDA[22] = +n3 * dz;
	delDA[23] = n1 * dx + n3 * dz;
	delDA[24] = -n1 * dx + n2 * dy + n3 * dz;
	delDA[25] = n2 * dy + n3 * dz;
	delDA[26] = n1 * dx + n2 * dy + n3 * dz;
	LOGI("before gen surface: nr:%d neu %d n5=%d",nr,neu,n5);
	if (neu)
		gen_surface(neu);
	return true;
}

void FourXle::deleteLists() {
	/*! deletes the display lists of the fourier maps.
	 */
	// TODO replace display lists
	/*for (int fac = 0; fac < 18; fac++) {
	 if ((chgl->foubas[fac]) && (glIsList(chgl->foubas[fac]))) {
	 //printf("deleting list %d %d %d\n", chgl->foubas[fac], glIsList(chgl->foubas[fac]),fac);
	 glDeleteLists(chgl->foubas[fac], 1);
	 chgl->foubas[fac] = 0;
	 }
	 }*/
}

void FourXle::trimm(char s[]) {
	/*! a trimm function for c-strings.
	 */
	char sc[409];
	int j = 0;
	int len = strlen(s);
	int start = 0;
	if (strstr(s, "SYMM") != NULL)
		start = 4;
	strncpy(sc, s, 400);
	for (int i = start; i < len; i++)
		if ((sc[i] != '\'') && (!isspace(sc[i])))
			s[j++] = toupper(sc[i]);
	s[j] = '\0';
}

void FourXle::deletes(char *s, int count) {
	/*! deletes count characters at the begining of s.
	 */
	if ((s == NULL) || (count < 1) || ((size_t) count > strlen(s)))
		return;
	for (int i = 0; i < count; i++)
		s[i] = ' ';
	trimm(s);
}

int FourXle::readHeader(const char *filename) {
	/*! reads the header of an fcf file
	 */
	FILE *f = NULL;
	char line[122], *dum = NULL;
	//size_t zlen=120;
	int ok = 0;
	int i;
	double T, V;
	f = fopen(filename, "r");
	if (f == NULL)
		return 3;
	ns = 0;
	sy[0][ns] = 1.0;
	sy[1][ns] = 0.0;
	sy[2][ns] = 0.0;

	sy[3][ns] = 0.0;
	sy[4][ns] = 1.0;
	sy[5][ns] = 0.0;

	sy[6][ns] = 0.0;
	sy[7][ns] = 0.0;
	sy[8][ns] = 1.0;

	sy[9][ns] = 0.0;
	sy[10][ns] = 0.0;
	sy[11][ns] = 0.0;
	ns = 1;
	int listcode = 0;
	do {
		dum = fgets(line, 120, f);
		if (dum == NULL) {
			fclose(f);
			return 2;
		};
		if (feof(f)) {
			fclose(f);
			return 2;
		};
		//LOGI("-->%s<-- %d",line,strncmp(dum, "_cell_length_a", 14));
		while (dum[0] == ' ')
			dum++;
		if (!strncmp(dum, "_shelx_title", 12)) {
			sscanf(line, "_shelx_title %[^\r\n]", titl);
			trimm(titl);
		}

		if (!strncmp(dum, "_shelx_refln_list_code", 22)) {
			sscanf(line, "_shelx_refln_list_code %d", &listcode);
			//qDebug()<<listcode;
			if (listcode != 6) {
				fclose(f);
				return 1;
			}
		}
		if (!strncmp(dum, "_cell_length_a", 14)) {
			sscanf(line, "_cell_length_a %f", &C[0]);
		}
		if (!strncmp(dum, "_cell_length_b", 14)) {
			sscanf(line, "_cell_length_b %f", &C[1]);
		}
		if (!strncmp(dum, "_cell_length_c", 14)) {
			sscanf(line, "_cell_length_c %f", &C[2]);
		}
		if (!strncmp(dum, "_cell_angle_alpha", 17)) {
			sscanf(line, "_cell_angle_alpha %f", &C[3]);
		}
		if (!strncmp(dum, "_cell_angle_beta", 16)) {
			sscanf(line, "_cell_angle_beta %f", &C[4]);
		}
		if (!strncmp(dum, "_cell_angle_gamma", 17)) {
			sscanf(line, "_cell_angle_gamma %f", &C[5]);
			for (i = 0; i < 3; i++) {
				if (C[i] < 0.1)
					return 2;
				T = .0174533 * C[i + 3];
				if (T < 0.001)
					return 2;
				D[i] = sin(T);
				D[i + 3] = cos(T);
				C[i + 6] = (D[i] / (C[i] * C[i]));
			}
			V = 1. - D[3] * D[3] - D[4] * D[4] - D[5] * D[5]
					+ 2. * D[3] * D[4] * D[5];
			C[6] /= V;
			C[7] /= V;
			C[8] /= V;
			C[9] = 2. * sqrt(C[7] * C[8]) * (D[4] * D[5] - D[3])
					/ (D[2] * D[2]);
			C[10] = 2. * sqrt(C[6] * C[8]) * (D[3] * D[5] - D[4])
					/ (D[0] * D[2]);
			C[11] = 2. * sqrt(C[6] * C[7]) * (D[3] * D[4] - D[5])
					/ (D[0] * D[1]);
			C[12] = C[2] * ((D[3] - D[4] * D[5]) / D[2]);
			C[13] = sqrt(V);
			C[14] = C[1] * C[2] * C[0] * sqrt(V);
			D[6] = C[1] * C[2] * D[0] / C[14];
			D[7] = C[0] * C[2] * D[1] / C[14];
			D[8] = C[0] * C[1] * D[2] / C[14];

		}
		if ((!strncmp(dum, "_symmetry_equiv_pos_as_xyz", 26))
				|| (!strncmp(dum, "_space_group_symop_operation_xyz", 32))) {
//      char s1[50],s2[50],s3[50];
//      char *kill=NULL,*nom=NULL,*div=NULL ;
			dum = fgets(line, 120, f);
			trimm(line);
			////
			while (strchr(line,'Y')) {
			//LOGI("-=>%s<=-",line);
			char s1[50], s2[50], s3[50];
			char *kill, *nom, *div;
			sscanf(line, "%[^,],%[^,],%s", s1, s2, s3);
			trimm(s1);
			trimm(s2);
			trimm(s3);
			//LOGI("(%s) (%s) (%s)",s1,s2,s3);
			sy[0][ns] = (NULL != (kill = strstr(s1, "-X"))) ? -1 :
						(NULL != (kill = strstr(s1, "X"))) ? +1 : 0;
			if (kill != NULL)
				((kill[0] == '-') || (kill[-1] == '+')) ?
						deletes(kill, 2) : deletes(kill, 1);
			sy[1][ns] = (NULL != (kill = strstr(s1, "-Y"))) ? -1 :
						(NULL != (kill = strstr(s1, "Y"))) ? +1 : 0;
			if (kill != NULL)
				((kill[0] == '-') || (kill[-1] == '+')) ?
						deletes(kill, 2) : deletes(kill, 1);
			sy[2][ns] = (NULL != (kill = strstr(s1, "-Z"))) ? -1 :
						(NULL != (kill = strstr(s1, "Z"))) ? +1 : 0;
			if (kill != NULL)
				((kill[0] == '-') || (kill[-1] == '+')) ?
						deletes(kill, 2) : deletes(kill, 1);

			sy[3][ns] = (NULL != (kill = strstr(s2, "-X"))) ? -1 :
						(NULL != (kill = strstr(s2, "X"))) ? +1 : 0;
			if (kill != NULL)
				((kill[0] == '-') || (kill[-1] == '+')) ?
						deletes(kill, 2) : deletes(kill, 1);
			sy[4][ns] = (NULL != (kill = strstr(s2, "-Y"))) ? -1 :
						(NULL != (kill = strstr(s2, "Y"))) ? +1 : 0;
			if (kill != NULL)
				((kill[0] == '-') || (kill[-1] == '+')) ?
						deletes(kill, 2) : deletes(kill, 1);
			sy[5][ns] = (NULL != (kill = strstr(s2, "-Z"))) ? -1 :
						(NULL != (kill = strstr(s2, "Z"))) ? +1 : 0;
			if (kill != NULL)
				((kill[0] == '-') || (kill[-1] == '+')) ?
						deletes(kill, 2) : deletes(kill, 1);

			sy[6][ns] = (NULL != (kill = strstr(s3, "-X"))) ? -1 :
						(NULL != (kill = strstr(s3, "X"))) ? +1 : 0;
			if (kill != NULL)
				((kill[0] == '-') || (kill[-1] == '+')) ?
						deletes(kill, 2) : deletes(kill, 1);
			sy[7][ns] = (NULL != (kill = strstr(s3, "-Y"))) ? -1 :
						(NULL != (kill = strstr(s3, "Y"))) ? +1 : 0;
			if (kill != NULL)
				((kill[0] == '-') || (kill[-1] == '+')) ?
						deletes(kill, 2) : deletes(kill, 1);
			sy[8][ns] = (NULL != (kill = strstr(s3, "-Z"))) ? -1 :
						(NULL != (kill = strstr(s3, "Z"))) ? +1 : 0;
			if (kill != NULL)
				((kill[0] == '-') || (kill[-1] == '+')) ?
						deletes(kill, 2) : deletes(kill, 1);
			if (NULL != (kill = strstr(s1, "/"))) {
				kill[0] = '\0';
				div = kill + 1;
				nom = s1;
				sy[9][ns] = atof(nom) / atof(div);
			} else
				sy[9][ns] = atof(s1);
			if (NULL != (kill = strstr(s2, "/"))) {
				kill[0] = '\0';
				div = kill + 1;
				nom = s2;
				sy[10][ns] = atof(nom) / atof(div);
			} else
				sy[10][ns] = atof(s2);

			if (NULL != (kill = strstr(s3, "/"))) {
				kill[0] = '\0';
				div = kill + 1;
				nom = s3;
				sy[11][ns] = atof(nom) / atof(div);
			} else
				sy[11][ns] = atof(s3);
			strcpy(line, "");
			dum = fgets(line, 120, f);
			trimm(line);
			LOGI("Symm %d:\n%g %g %g  %g\n%g %g %g  %g\n%g %g %g  %g\n",ns,
					sy[0][ns],sy[1][ns],sy[2][ns],sy[9][ns],
					sy[3][ns],sy[4][ns],sy[5][ns],sy[10][ns],
					sy[6][ns],sy[7][ns],sy[8][ns],sy[11][ns]
			);
			ns++;
		}
		}
		if (!strncmp(dum, "_refln_phase_calc", 17))
			ok = 1;
	} while ((!ok) && (!feof(f)));

	if (listcode != 6)
		return 1;
	for (int i = 0; i < ns; i++) {
		for (int n = i + 1; n < ns; n++) {
			int u = 0, v = 0;
			for (int j = 0; j < 9; j++) {
				u += abs(sy[j][n] - sy[j][i]);
				v += abs(sy[j][n] + sy[j][i]);
			}
			if (fmin(u, v) > 0.01)
				continue;
			for (int j = 0; j < 12; j++) {
				sy[j][n] = sy[j][ns - 1];
			}
			ns--;
		}
	}
	fclose(f);
	//LOGI("cell %g %g %g %g %g %g %g\n",C[0],C[1],C[2],C[3],C[4],C[5],C[14]);

	return 0;
}

void FourXle::sorthkl(int nr, Rec r[]) {
	/*! sorts the reflection list
	 */
	Rec *hilf = (Rec*) malloc(sizeof(Rec) * nr);
	int i, j, k, nj, ni, spalte;
	int index[4096];
	for (spalte = 0; spalte < 3; spalte++) {
		j = -999999;
		k = 999999;
		switch (spalte) {
		case 0:
			for (i = 0; i < nr; i++) {
				j = (j < r[i].ih) ? r[i].ih : j;
				k = (k > r[i].ih) ? r[i].ih : k;
			}
			break;
		case 1:
			for (i = 0; i < nr; i++) {
				j = (j < r[i].ik) ? r[i].ik : j;
				k = (k > r[i].ik) ? r[i].ik : k;
			}
			break;
		case 2:
			for (i = 0; i < nr; i++) {
				j = (j < r[i].il) ? r[i].il : j;
				k = (k > r[i].il) ? r[i].il : k;
			}
			break;
		}
		nj = -k;
		ni = (nj + j + 1);
		for (i = 0; i <= ni; i++)
			index[i] = 0;
		for (i = 0; i < nr; i++) {
			switch (spalte) {
			case 0:
				j = r[i].ih + nj;
				break;
			case 1:
				j = r[i].ik + nj;
				break;
			case 2:
				j = r[i].il + nj;
				break;
			}
			index[j]++;/*brauch ich das? -->JA!*/
			hilf[i].ih = r[i].ih;
			hilf[i].ik = r[i].ik;
			hilf[i].il = r[i].il;
			hilf[i].fo = r[i].fo;
			hilf[i].so = r[i].so;
			hilf[i].fc = r[i].fc;
			hilf[i].phi = r[i].phi;
		}/*/4*/
		j = 0;
		for (i = 0; i < ni; i++) {
			k = j;
			j += index[i];
			index[i] = k;
		}/*/5*/
		for (i = 0; i < nr; i++) {
			switch (spalte) {
			case 0:
				j = hilf[i].ih + nj;
				break;
			case 1:
				j = hilf[i].ik + nj;
				break;
			case 2:
				j = hilf[i].il + nj;
				break;
			}
			index[j]++;
			j = index[j] - 1;
			r[j].ih = hilf[i].ih;
			r[j].ik = hilf[i].ik;
			r[j].il = hilf[i].il;
			r[j].fo = hilf[i].fo;
			r[j].so = hilf[i].so;
			r[j].fc = hilf[i].fc;
			r[j].phi = hilf[i].phi;
		}/*/6*/
	}/*/spalten*/
	free(hilf);
}
/*
 void FourXle::bewegt(V3 nm) {/*!moves the rotation center to nm* /

 V3 v , alturs=urs;
 = cart2fracnm,v);
 urs=V3(1,1,1)-1.0*v;
 = frac2cart( urs,urs);
 if ((chgl->objCnt==acnt)&&(maptrunc==2)) return;
 if (urs==alturs) return;
 balken->setMinimum(0);
 balken->setMaximum(n3*6);
 balken->show();
 balkenstep=0;
 gen_surface(false);

 }

 void FourXle::inimap() {/*! reinitializes the display lists for screenshots* /
 //for screenies
 deleteLists();
 gen_surface(false);
 balken->hide();
 }*/

void FourXle::gen_surface(bool neu, int imin, int imax) {
	if (noGoMap == NULL)
		return;
	/*! creates iso surfaces for fo-fc- fo-fc+ fo+ and fo- maps if neu then the iso values are calculated fro the sigma value of the map.
	 *
	 */
	imax = qMin(4, imax);
	/*if ((chgl->centerSelection->isChecked())
	 && (mole->selectedatoms.isEmpty())) {
	 V3 v;
	 = cart2fracchgl->altemitte, v);
	 urs = V3(1, 1, 1) - 1.0 * v;
	 = frac2cart( urs, urs);
	 } else {
	 V3 v = V3(0, 0, 0);
	 for (int i = 0; i < mole->selectedatoms.size(); i++)
	 v += mole->selectedatoms[i].pos;
	 v *= 1.0 / mole->selectedatoms.size();
	 v = cart2fracchgl->altemitte);
	 urs = V3(1, 1, 1) - 1.0 * v;
	 urs = frac2cart( urs, urs);
	 }*/
	for (int no = 0; no < (n5 * 27); no++)
		noGoMap[no] = 0;
	dxc = cart2frac(dx);
	dyc = cart2frac(dy);
	dzc = cart2frac(dz);
	int incx = (int) (1.4 / dx.x);
	int incy = (int) (1.4 / dy.y);
	int incz = (int) (1.4 / dz.z);
	int incmin = qMin(incx, qMin(incy, incz));
	incmin *= incmin;
	for (int g = 3; g < nPos; g+=3) {
		oc = cart2frac(V3(fraci[g],fraci[g+1],fraci[g+2]));
		//oc = V3(fraci[g],fraci[g+1],fraci[g+2]);
		//LOGI("%g %g %g  %g %g %g\n",oc.x,oc.y,oc.z, posi[g],posi[g+1],posi[g+2]);
		int ax = (int) ((oc.x) / dxc.x - 0.499), bx = (int) ((oc.y) / dyc.y
				- 0.499), cx = (int) ((oc.z) / dzc.z - 0.499);
		for (int aa = ax - incx; aa < ax + incx; aa++) {
			for (int bb = bx - incy; bb < bx + incy; bb++) {
				for (int cc = cx - incz; cc < cx + incz; cc++) {
					if (incmin
							< ((aa - ax) * (aa - ax) + (bb - bx) * (bb - bx)
									+ (cc - cx) * (cc - cx)))
						continue;
					int dEx = dex3(aa, bb, cc);
					if ((dEx > 0) && (dEx < 27 * n5))
						noGoMap[dEx] = 1;
				}
			}
		}
	}
//  oldatomsize=mole->showatoms.size();
//  }
	tri = 0;
	for (int fac = imin; fac < imax; fac++) {
		//	if ((chgl->foubas[fac]) && (glIsList(chgl->foubas[fac])))
		//	glDeleteLists(chgl->foubas[fac], 1);

		switch (fac) {
		case 0:
			if (neu)
				iso[1] = sigma[0] * 2.7;
			else
				iso[1] = fabs(iso[1]);
			mtyp = 1;
			break;

		case 1:
			if (neu)
				iso[1] = -sigma[0] * 2.7;
			else
				iso[1] = -fabs(iso[1]);
			mtyp = 1;
			break;
		case 2:
			if (neu)
				iso[0] = sigma[1] * 1.2;
			//printf("blau %g %d\n",iso[0],neu);

			else
				iso[0] = fabs(iso[0]);
			mtyp = 0;
			break;
		case 3:
			if (neu)
				iso[0] = -sigma[1] * 1.2;
			else
				iso[0] = -fabs(iso[0]);
			//printf("orange %g %d\n",iso[0],neu);
			mtyp = 0;
			break;
		}
        LOGI("iso1 %g iso2 %g %g %g\n",iso[0],iso[1],sigma[0],sigma[1]);
		int ix, iy, iz;
		for (iz = 0; iz < n3; iz++) {
			for (iy = 0; iy < n2; iy++) {
				for (ix = 0; ix < n1; ix++) {
					CalcVertex(ix, iy, iz);
				}
			}
		}
		/*if (!chgl->foubas[fac])
		 chgl->foubas[fac] = glGenLists(1);
		 glNewList(chgl->foubas[fac], GL_COMPILE);

		 glPushMatrix();
		 glScaled(chgl->L, chgl->L, chgl->L);
		 */
		int h, k, l;
		for (h = 0; h < n1; h++) {
			for (k = 0; k < n2; k++) {
				for (l = 0; l < n3; l++) {
					MakeElement(h, k, l, n1, n4, fac);
				}
			}
		}
		//glPopMatrix();
		//glEndList();
	}
	iso[0] = fabs(iso[0]);

//acnt=chgl->objCnt;
}

void FourXle::change_iso(int numsteps, int diff) {
	/*! canges the iso value of the fo or fo-fc maps and redraws them
	 */
	iso[diff] = fabs(iso[diff]);
	iso[diff] += iso[diff] * numsteps / 10.0;
	int mi = 0, ma = 5;
	switch (diff) {
	case 0:
		mi = 2;
		ma = 4;
		break;
	case 1:
		mi = 0;
		ma = 2;
		break;

	}
	gen_surface(false, mi, ma);
}

V3 FourXle::frac2cart(V3 x) {
	V3 y;
	float u[9];
	u[0] = C[0];
	u[1] = C[1] * D[5];
	u[2] = C[2] * D[4];

	u[3] = 0;
	u[4] = C[1] * D[2];
	u[5] = C[12];

	u[6] = 0;
	u[7] = 0;
	u[8] = C[2] * C[13] / D[2];
	y.x = x.x * u[0] + x.y * u[1] + x.z * u[2];
	y.y = x.x * u[3] + x.y * u[4] + x.z * u[5];
	y.z = x.x * u[6] + x.y * u[7] + x.z * u[8];

	return y;
}
V3 FourXle::cart2frac(V3 x) {
	V3 y;

	float u[9];
	u[0] = 1.0 / C[0];
	u[1] = 0.0;
	u[2] = 0.0;
	u[3] = -1.0 / (C[0] * tan(C[5]/180.0*M_PI));
	u[4] = 1.0 / (C[1] * D[2]);
	u[5] = 0.0;
	u[6] = (D[3] * D[5] - D[4]) / (C[0] * C[13] * D[2]);
	u[7] = (D[4] * D[5] - D[3]) / (C[1] * C[13] * D[2]);
	u[8] = D[2] / (C[2] * C[13]);
	y.x = x.x * u[0] + x.y * u[3] + x.z * u[6];
	y.y = x.x * u[1] + x.y * u[4] + x.z * u[7];
	y.z = x.x * u[2] + x.y * u[5] + x.z * u[8];
	return y;
}

void FourXle::CalcVertex(int ix, int iy, int iz) {
	V3 urs = V3(fraci[0], fraci[1], fraci[2]);
	V3 mdz = (0.5 * dx) + (0.5 * dy) + (0.5 * dz);
	//mdz *=2.25;
	V3 o, fl, m2u =
			V3(0.5, 0.5, 0.5);//
			//V3(0.0, 0.0, 0.0);
	m2u = frac2cart(m2u);
	float vo = 0, vx = 0, vy = 0, vz = 0;
	int idx = dex(ix, iy, iz);
	nodex[idx].flag = 0;
	nodey[idx].flag = 0;
	nodez[idx].flag = 0;
	if (mtyp == 0) {      //*datfo,*datfo_fc,*datf1_f2
		vo = datfo[idx] - iso[mtyp];
		vx = datfo[dex(ix + 1, iy, iz)] - iso[mtyp];
		vy = datfo[dex(ix, iy + 1, iz)] - iso[mtyp];
		vz = datfo[dex(ix, iy, iz + 1)] - iso[mtyp];
	} else {
		vo = datfo_fc[idx] - iso[mtyp];
		vx = datfo_fc[dex(ix + 1, iy, iz)] - iso[mtyp];
		vy = datfo_fc[dex(ix, iy + 1, iz)] - iso[mtyp];
		vz = datfo_fc[dex(ix, iy, iz + 1)] - iso[mtyp];
	}
	V3 nor = V3(0, 0, 0);      //Normalize((vx-vo)*dx+(vy-vo)*dy+(vz-vo)*dz);
	if (Intersect(vo, vx)) {
		o = dx * ((vo / (vo - vx)) + ix) + dy * iy + dz * iz + m2u;

		o = cart2frac(o);
		o += V3(-0.5, -0.5, -0.5);
		fl = V3(floor(o.x), floor(o.y), floor(o.z));
		o += -1.0 * fl;
		o += V3(0.5, 0.5, 0.5);
		o = frac2cart(o);
		o += -1.0 * m2u;
		o += mdz;
		o += 1.0 *urs;
//    orte.append(o);
		nodex[idx].vertex = o;
//		nodex[idx].normal = nor;
		nodex[idx].flag = 1;
	}
	if (Intersect(vo, vy)) {
		o = dx * ix + dy * ((vo / (vo - vy)) + iy) + dz * iz + m2u;
		o = cart2frac(o);
		o += V3(-0.5, -0.5, -0.5);
		fl = V3(floor(o.x), floor(o.y), floor(o.z));
		o += -1.0 * fl;
		o += V3(0.5, 0.5, 0.5);
		o = frac2cart(o);
		o += -1.0 * m2u;
		o += mdz;
		o += 1.0 *urs;
//    orte.append(o);
		nodey[idx].vertex = o;
//		nodey[idx].normal = nor;
		nodey[idx].flag = 1;
	}
	if (Intersect(vo, vz)) {
		o = dx * ix + dy * iy + dz * ((vo / (vo - vz)) + iz) + m2u;
		o = cart2frac(o);
		o += V3(-0.5, -0.5, -0.5);
		fl = V3(floor(o.x), floor(o.y), floor(o.z));
		o += -1.0 * fl;
		o += V3(0.5, 0.5, 0.5);
		o = frac2cart(o);
		o += -1.0 * m2u;
		o += mdz;
		o += 1.0 *urs;
//    orte.append(o);
		nodez[idx].vertex = o;
//		nodez[idx].normal = nor;
		nodez[idx].flag = 1;
	}
}

V3& FourXle::VectorSelected(FNode& node0, FNode& node1, FNode& node2,
		FNode& node3) {
	if (node1 && node2 && node3) {
		float d1 = Distance(node0.vertex, node1.vertex)
				+ Distance(node3.vertex, node2.vertex);
		float d2 = Distance(node0.vertex, node2.vertex)
				+ Distance(node3.vertex, node1.vertex);
		if (d1 > d2)
			return node2.vertex;
		else
			return node1.vertex;
	} else {
		if (node1)
			return node1.vertex;
		else if (node2)
			return node2.vertex;
		else if (node3)
			return node3.vertex;
	}
	return node0.vertex;
}

void FourXle::makeFaces(int n, FNode poly[], int fac) {
	int sign[12];
	if (n < 3)
		return;  //weniger als 3 verts -> nichts zu tun
	V3 mid_ver = V3(0, 0, 0);
	V3 mid_nor = V3(0, 0, 0);
//V3 mid_nor2 = V3(0,0,0);
	for (int i = 0; i < n; i++) {
		mid_ver += poly[i].vertex;
	}
	mid_ver *= (1.0 / n);
	//V3 mit = V3(1, 1, 1);
	//mit = frac2cart(mit);
	for (int w = 0; w < 27; w++) {
		{
			oc = cart2frac(mid_ver + delDA[w]);
			int     ax = (int) ((oc.x) / dxc.x - 0.499),
					bx = (int) ((oc.y) / dyc.y - 0.499),
					cx = (int) ((oc.z) / dzc.z - 0.499);
			int DEX = dex3(ax, bx, cx);
			if (!noGoMap[DEX])
				continue;
		}
		if (nFoPl>(nobs*100000-12)){
			foPlus  = (float*) realloc(foPlus,  sizeof(float) * ++nobs*100000);
			LOGI("foPlus reallocated to %d xx %d  ",nFoPl, nobs*100000);
		}
		if (nFoMi>nobsm*100000-12){
		    foMinus = (float*) realloc(foMinus, sizeof(float) * ++nobsm*100000);
		LOGI("foMinus reallocated to %d bytes ",sizeof(float) * nobsm*100000);
	}
		if (nDiMi>(ndifm*100000-12)){
		    diMinus = (float*) realloc(diMinus, sizeof(float) * ++ndifm*100000);
		LOGI("diMinus reallocated to %d bytes ",sizeof(float) * ndifm*100000);
	}
		if (nDiPl>ndifp*100000-12){
		    diPlus  = (float*) realloc(diPlus,  sizeof(float) * ++ndifp*100000);
		LOGI("diPlus reallocated to %d %d ", ndifp*100000,nDiPl);
	}
		switch (fac) {
		case 2:

			for (int k = 0; k <= n; k++) {
				foPlus[nFoPl++] = mid_ver.x + delDA[w].x;
				foPlus[nFoPl++] = mid_ver.y + delDA[w].y;
				foPlus[nFoPl++] = mid_ver.z + delDA[w].z;
				foPlus[nFoPl++] = poly[k % n].vertex.x + delDA[w].x;
				foPlus[nFoPl++] = poly[k % n].vertex.y + delDA[w].y;
				foPlus[nFoPl++] = poly[k % n].vertex.z + delDA[w].z;

				foPlus[nFoPl++] = poly[k % n].vertex.x + delDA[w].x;
				foPlus[nFoPl++] = poly[k % n].vertex.y + delDA[w].y;
				foPlus[nFoPl++] = poly[k % n].vertex.z + delDA[w].z;
				foPlus[nFoPl++] = poly[(k+1) % n].vertex.x + delDA[w].x;
				foPlus[nFoPl++] = poly[(k+1) % n].vertex.y + delDA[w].y;
				foPlus[nFoPl++] = poly[(k+1) % n].vertex.z + delDA[w].z;

			}
			break;
		case 3:
			for (int k = 0; k <= n; k++) {
				foMinus[nFoMi++] = mid_ver.x + delDA[w].x;
				foMinus[nFoMi++] = mid_ver.y + delDA[w].y;
				foMinus[nFoMi++] = mid_ver.z + delDA[w].z;
				foMinus[nFoMi++] = poly[k % n].vertex.x + delDA[w].x;
				foMinus[nFoMi++] = poly[k % n].vertex.y + delDA[w].y;
				foMinus[nFoMi++] = poly[k % n].vertex.z + delDA[w].z;

				foMinus[nFoMi++] = poly[k % n].vertex.x + delDA[w].x;
				foMinus[nFoMi++] = poly[k % n].vertex.y + delDA[w].y;
				foMinus[nFoMi++] = poly[k % n].vertex.z + delDA[w].z;
				foMinus[nFoMi++] = poly[(k+1) % n].vertex.x + delDA[w].x;
				foMinus[nFoMi++] = poly[(k+1) % n].vertex.y + delDA[w].y;
				foMinus[nFoMi++] = poly[(k+1) % n].vertex.z + delDA[w].z;

			}

			break;
		case 1:
			for (int k = 0; k <= n; k++) {
				diMinus[nDiMi++] = mid_ver.x + delDA[w].x;
				diMinus[nDiMi++] = mid_ver.y + delDA[w].y;
				diMinus[nDiMi++] = mid_ver.z + delDA[w].z;
				diMinus[nDiMi++] = poly[k % n].vertex.x + delDA[w].x;
				diMinus[nDiMi++] = poly[k % n].vertex.y + delDA[w].y;
				diMinus[nDiMi++] = poly[k % n].vertex.z + delDA[w].z;

				diMinus[nDiMi++] = poly[k % n].vertex.x + delDA[w].x;
				diMinus[nDiMi++] = poly[k % n].vertex.y + delDA[w].y;
				diMinus[nDiMi++] = poly[k % n].vertex.z + delDA[w].z;
				diMinus[nDiMi++] = poly[(k+1) % n].vertex.x + delDA[w].x;
				diMinus[nDiMi++] = poly[(k+1) % n].vertex.y + delDA[w].y;
				diMinus[nDiMi++] = poly[(k+1) % n].vertex.z + delDA[w].z;
			}
			break;
		case 0:
			for (int k = 0; k <= n; k++) {
				diPlus[nDiPl++] = mid_ver.x + delDA[w].x;
				diPlus[nDiPl++] = mid_ver.y + delDA[w].y;
				diPlus[nDiPl++] = mid_ver.z + delDA[w].z;
				diPlus[nDiPl++] = poly[k % n].vertex.x + delDA[w].x;
				diPlus[nDiPl++] = poly[k % n].vertex.y + delDA[w].y;
				diPlus[nDiPl++] = poly[k % n].vertex.z + delDA[w].z;

				diPlus[nDiPl++] = poly[k % n].vertex.x + delDA[w].x;
				diPlus[nDiPl++] = poly[k % n].vertex.y + delDA[w].y;
			    diPlus[nDiPl++] = poly[k % n].vertex.z + delDA[w].z;
			    diPlus[nDiPl++] = poly[(k+1) % n].vertex.x + delDA[w].x;
				diPlus[nDiPl++] = poly[(k+1) % n].vertex.y + delDA[w].y;
				diPlus[nDiPl++] = poly[(k+1) % n].vertex.z + delDA[w].z;
			}
			break;
		default:
			break;
		}
		//}else LOGI("ende!");
	}
}  //omp

int FourXle::IndexSelected(FNode& node0, FNode& node1, FNode& node2,
		FNode& node3) {
	if (node1 && node2 && node3) {
		float d1 = Distance(node0.vertex, node1.vertex)
				+ Distance(node3.vertex, node2.vertex);
		float d2 = Distance(node0.vertex, node2.vertex)
				+ Distance(node3.vertex, node1.vertex);
		if (d1 > d2)
			return 2;
		else
			return 1;
	} else {
		if (node1)
			return 1;
		else if (node2)
			return 2;
		else if (node3)
			return 3;
	}
	return 0;

}

void FourXle::MakeElement(int ix, int iy, int iz, int s1, int s2, int fac) {
	int conn[12][2][4] = { { { 0, 1, 7, 6 }, { 0, 2, 8, 3 } },  //  0
			{ { 1, 2, 5, 4 }, { 1, 0, 6, 7 } },  //  1
			{ { 2, 0, 3, 8 }, { 2, 1, 4, 5 } },  //  2
			{ { 3, 8, 2, 0 }, { 3, 4, 10, 9 } },  //  3
			{ { 4, 3, 9, 10 }, { 4, 5, 2, 1 } },  //  4
			{ { 5, 4, 1, 2 }, { 5, 6, 9, 11 } },  //  5
			{ { 6, 5, 11, 9 }, { 6, 7, 1, 0 } },  //  6
			{ { 7, 6, 0, 1 }, { 7, 8, 11, 10 } },  //  7
			{ { 8, 7, 10, 11 }, { 8, 3, 0, 2 } },  //  8
			{ { 9, 10, 4, 3 }, { 9, 11, 5, 6 } },  //  9
			{ { 10, 11, 8, 7 }, { 10, 9, 3, 4 } },  // 10
			{ { 11, 9, 6, 5 }, { 11, 10, 7, 8 } }   // 11
	};
	FNode node[12];
	FNode polygon[12];
	node[0] = nodex[(ix + iy * s1 + iz * s2) % n5];        // 000x
	node[1] = nodey[(ix + iy * s1 + iz * s2) % n5];        // 000y
	node[2] = nodez[(ix + iy * s1 + iz * s2) % n5];        // 000z
	node[3] = nodex[(ix + iy * s1 + ((iz + 1) % n3) * s2) % n5];    // 001y
	node[4] = nodey[(ix + iy * s1 + ((iz + 1) % n3) * s2) % n5];    // 001z
	node[5] = nodez[(ix + ((iy + 1) % n2) * s1 + iz * s2) % n5];    // 010x
	node[6] = nodex[(ix + ((iy + 1) % n2) * s1 + iz * s2) % n5];    // 010y
	node[7] = nodey[(((1 + ix) % n1) + iy * s1 + iz * s2) % n5];      // 100y
	node[8] = nodez[(((1 + ix) % n1) + iy * s1 + iz * s2) % n5];      // 100z
	node[9] = nodex[(ix + ((iy + 1) % n2) * s1 + ((iz + 1) % n3) * s2) % n5]; // 011x
	node[10] = nodey[(((ix + 1) % n1) + iy * s1 + ((iz + 1) % n3) * s2) % n5]; // 101y
	node[11] = nodez[(((ix + 1) % n1) + ((iy + 1) % n2) * s1 + iz * s2) % n5]; // 110z
	if (((char) node[0] + node[1] + node[2] + node[3] + node[4] + node[5]
			+ node[6] + node[7] + node[8] + node[9] + node[10] + node[11]) == 0)
		return;
	for (int is = 0; is < 12; is++) {
		if (!node[is])
			continue;

		int n = 0, i = is, m = 0;      //,ai=i;
		float dis;
		dis = 0;
		do {
			polygon[n++] = node[i];
			int sol = IndexSelected(node[conn[i][m][0]], node[conn[i][m][1]],
					node[conn[i][m][2]], node[conn[i][m][3]]);
			//ai=i;
			i = conn[i][m][sol];
			if (sol == 2)
				m ^= 1;
			dis += Distance(polygon[0].vertex, node[i].vertex);
			node[i].flag = 0;
		} while ((i != is) && (n < 11));
		if (n >= 3) {
			if (dis < 5)
				makeFaces(n, polygon, fac);
			else {
				int axe = 0;
				double delx = 0, dely = 0, delz = 0;
				double mind = 100000000;
				V3 minp = V3(10000, 10000, 10000), lihiun = V3(-10000, -10000,
						-10000);
				int minii = 0;
				for (int polni = 1; polni <= n; polni++) {
					delx += fabs(
							polygon[polni - 1].vertex.x
									- polygon[polni % n].vertex.x);
					dely += fabs(
							polygon[polni - 1].vertex.y
									- polygon[polni % n].vertex.y);
					delz += fabs(
							polygon[polni - 1].vertex.z
									- polygon[polni % n].vertex.z);
					if (Distance(polygon[polni % n].vertex, lihiun) < mind) {
						mind = Distance(polygon[polni % n].vertex, minp);
						minii = polni % n;
					}
				}
				minp = polygon[minii].vertex;
				axe |= (delx > 1) ? 1 : 0;
				axe |= (dely > 1) ? 2 : 0;
				axe |= (delz > 1) ? 4 : 0;
				for (int polni = 0; polni < n; polni++) {
					V3 neo = polygon[polni].vertex;
					double lang = Distance(minp, neo);
					if ((lang > Distance(minp, neo + dx * n1)) && (axe & 1))
						neo += dx * n1;
					else if ((lang > Distance(minp, neo - dx * n1))
							&& (axe & 1))
						neo += -n1 * dx;
					lang = Distance(minp, neo);
					if ((lang > Distance(minp, neo + dy * n2)) && (axe & 2))
						neo += n2 * dy;
					else if ((lang > Distance(minp, neo - dy * n2))
							&& (axe & 2))
						neo += -n2 * dy;
					lang = Distance(minp, neo);
					if ((lang > Distance(minp, neo + n3 * dz)) && (axe & 4))
						neo += n3 * dz;
					else if ((lang > Distance(minp, neo - n3 * dz))
							&& (axe & 4))
						neo += -n3 * dz;
					polygon[polni].vertex = neo;
				}
				dis = 0;
				for (int polni = 1; polni <= n; polni++) {
					dis += Distance(polygon[polni - 1].vertex,
							polygon[polni % n].vertex);
				}
				if (dis < 5)
					makeFaces(n, polygon,fac);
			}
		}
	}

}

V3 FourXle::CalcNormalX(int ix, int iy, int iz) {
	V3 tang[4];
	tang[0] = VectorSelected(nodex[dex(ix, iy, iz)], nodey[dex(ix, iy, iz)],
			nodey[dex(ix + 1, iy, iz)], nodex[dex(ix, iy + 1, iz)]);
	tang[1] = VectorSelected(nodex[dex(ix, iy, iz)], nodey[dex(ix, iy - 1, iz)],
			nodey[dex(ix + 1, iy - 1, iz)], nodex[dex(ix, iy - 1, iz)]);
	tang[2] = VectorSelected(nodex[dex(ix, iy, iz)], nodez[dex(ix, iy, iz)],
			nodez[dex(ix + 1, iy, iz)], nodex[dex(ix, iy, iz + 1)]);
	tang[3] = VectorSelected(nodex[dex(ix, iy, iz)], nodez[dex(ix, iy, iz - 1)],
			nodez[dex(ix + 1, iy, iz - 1)], nodex[dex(ix, iy, iz - 1)]);
	return Normalize((tang[0] - tang[1]) % (tang[2] - tang[3]));
}

V3 FourXle::CalcNormalY(int ix, int iy, int iz) {
	V3 tang[4];
	tang[0] = VectorSelected(nodey[dex(ix, iy, iz)], nodex[dex(ix, iy, iz)],
			nodex[dex(ix, iy + 1, iz)], nodey[dex(ix + 1, iy, iz)]);
	tang[1] = VectorSelected(nodey[dex(ix, iy, iz)], nodex[dex(ix - 1, iy, iz)],
			nodex[dex(ix - 1, iy + 1, iz)], nodey[dex(ix - 1, iy, iz)]);
	tang[2] = VectorSelected(nodey[dex(ix, iy, iz)], nodez[dex(ix, iy, iz)],
			nodez[dex(ix, iy + 1, iz)], nodey[dex(ix, iy, iz + 1)]);
	tang[3] = VectorSelected(nodey[dex(ix, iy, iz)], nodez[dex(ix, iy, iz - 1)],
			nodez[dex(ix, iy + 1, iz - 1)], nodey[dex(ix, iy, iz - 1)]);
	return Normalize((tang[2] - tang[3]) % (tang[0] - tang[1]));
}

V3 FourXle::CalcNormalZ(int ix, int iy, int iz) {
	V3 tang[4];
	tang[0] = VectorSelected(nodez[dex(ix, iy, iz)], nodex[dex(ix, iy, iz)],
			nodex[dex(ix, iy, iz + 1)], nodez[dex(ix + 1, iy, iz)]);
	tang[1] = VectorSelected(nodez[dex(ix, iy, iz)], nodex[dex(ix - 1, iy, iz)],
			nodex[dex(ix - 1, iy, iz + 1)], nodez[dex(ix - 1, iy, iz)]);
	tang[2] = VectorSelected(nodez[dex(ix, iy, iz)], nodey[dex(ix, iy, iz)],
			nodey[dex(ix, iy, iz + 1)], nodez[dex(ix, iy + 1, iz)]);
	tang[3] = VectorSelected(nodez[dex(ix, iy, iz)], nodey[dex(ix, iy - 1, iz)],
			nodey[dex(ix, iy - 1, iz + 1)], nodez[dex(ix, iy - 1, iz)]);
	return Normalize((tang[0] - tang[1]) % (tang[2] - tang[3]));
}

inline int FourXle::dex(int x, int y, int z) {
	/*! dex is used to adress elemennts of a one dimensional array by three indizes like it is a 3 dimensional array
	 * @param x,y,z tree dimensional indices
	 * if x is < 0 or > n1 it is not a problem because % is used to clamp it.
	 * if y is < 0 or > n2 it is not a problem because % is used to clamp it.
	 * if z is < 0 or > n3 it is not a problem because % is used to clamp it.
	 * \returns index of an 1 dimensional array
	 */
	x = (x + n1) % n1;
	y = (y + n2) % n2;
	z = (z + n3) % n3;
	return x + n1 * (y + n2 * z);
}

inline int FourXle::dex3(int x, int y, int z) {
	int n31 = 3 * n1, n32 = 3 * n2, n33 = 3 * n3;
	x = (x + n31) % n31;
	y = (y + n32) % n32;
	z = (z + n33) % n33;
	return x + n31 * (y + n32 * z);
}
