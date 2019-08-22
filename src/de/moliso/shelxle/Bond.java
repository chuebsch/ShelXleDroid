package de.moliso.shelxle;
import de.moliso.shelxle.Vector3D;

public class Bond{
    Bond(Vector3D m, Vector3D n){
    	a=m;
    	b=n;
    };
    Vector3D a, b;
};