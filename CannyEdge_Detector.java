import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.Color;
import java.lang.Math;

public class CannyEdge_Detector{

	public static double RGBadjuster(double color){
		try{
			if(color>255.){
				color=255.;
			}else if(color<0.){
				color=0.;
			}
		}catch(Exception e){
			System.out.println("Error! code:101");
		}
		return color;
	}// ---------------------------------------- RGBadjuster

	public static void ImageWriter(String file_name,double mat8[][])
	                                               throws Exception{
		BufferedImage img = 
			new BufferedImage(mat8[0].length,mat8.length,BufferedImage.TYPE_INT_BGR);

		for(int iy=0; iy<img.getHeight(); iy++){
		for(int ix=0; ix<img.getWidth(); ix++){
			int iset = (int) RGBadjuster(mat8[iy][ix]);
			iset = (255<<24) + (iset<<16) + (iset<<8) + iset;
			img.setRGB(ix,iy,iset);
		}
		}
		boolean chk = ImageIO.write(img,"png",new File("res/"+file_name+".png") );
	}// ---------------------------------------- ImageWriter

	public static double[][] GrayScaling(BufferedImage inimg){
		double[][] gray = new double[inimg.getHeight()][inimg.getWidth()];
		double r,g,b;

		for(int iy=0; iy<inimg.getHeight(); iy++){
		for(int ix=0; ix<inimg.getWidth(); ix++){
			Color c = new Color( inimg.getRGB(ix,iy) );
			r = (double)c.getRed();	g = (double)c.getGreen(); b = (double)c.getBlue();

			
			double gam = 2.2;
			r = Math.pow(r,1.0/gam);	g = Math.pow(g,1.0/gam);b = Math.pow(b,1.0/gam);
			double v1 = 0.2126*r + 0.7152*g + 0.0722*b;
			double v = Math.pow(v1,gam);
			
			//double v = (r+g+b)/3.;

			gray[iy][ix] = RGBadjuster(v);
		}
		}
		return gray;
	}

	public static double[][] HysteresisThreshold( double[][] img,
	                                                double max, double min){
		double adjacent;
		int ny = img.length;	int nx = img[0].length;
		double[][] res = new double[ny][nx];

		for(int iy=0; iy<ny; iy++){
		for(int ix=0; ix<nx; ix++){
			if( !(iy==0 || ix==0 || iy==ny-1 || ix==nx-1) ){
				double num=img[iy][ix];

				if(num>=max){
					num=255.;

				}else if( min<=num && num<max){
					double max1 = 0.;
					for ( int i=-1; i<=1; i++ ){
					for( int j=-1; j<=1; j++){
						if( max1>=img[iy+i][ix+j] ) max1 = img[iy+i][ix+j];
					}
					}
					adjacent = max1;
					if(adjacent>=max){
						num=255.;
					}else{
						num=0.;
					}

				}else{
					num = 0.;

				}
				res[iy][ix] = num;

			}else{
				res[iy][ix] = img[iy][ix];
			}

		}
		}
		return res;
	}

	public static double[][] NonMaximumSuppression( double[][] sbABS,
		                                                double[][] sbAng ){
		double deg,max,min;
		double a[] = new double[3];
		double img[][] = new double[3][3];
		int ny = sbABS.length;	int nx = sbABS[0].length;
		double[][] res = new double[ny][nx];

		for(int iy=0; iy<ny; iy++){
		for(int ix=0; ix<nx; ix++){
			if( !(iy==0 || ix==0 || iy==ny-1 || ix==nx-1) ){
				for ( int i=-1; i<=1; i++ ){
				for( int j=-1; j<=1; j++){
					img[i+1][j+1] = sbABS[iy+i][ix+j];
				}
				}
				max = 0.;

				deg = sbAng[iy][ix]*180./Math.PI;
				if( -22.5<=deg && deg<22.5){ // LR -
					max = 0.;
					for( int i=-1;i<2;i++){
						if( img[1][1+i]>max ){max = img[1][1+i];}
					}
				}
				else if( 22.5<=deg && deg<67.5){ // RULD /
					max = 0.;
					for( int i=-1;i<2;i++){
						if( img[1+i][1+i]>max ){max = img[1+i][1+i];}
					}
				}
				else if( 67.5<=deg && deg<112.5){ // UD |
					max = 0.;
					for( int i=-1;i<2;i++){
						if( img[1+i][1]>max ){max = img[1+i][1];}
					}
				}/*
				else if( 112.5<=deg && deg<157.5){ // LURD \
	
				}
				else if( 157.5<=deg && deg<202.5){ // LR -
	
				}*/
				if( img[1][1]!=max ){
					res[iy][ix] = 0.;
				} else{
					res[iy][ix] = img[1][1];
				}
			}else{
				res[iy][ix] = sbABS[iy][ix];
			}

		}
		}
		return res;

	}

	public static double[][] Angle( double[][] xMat, double[][] yMat){
		int ny = xMat.length;	int nx = xMat[0].length;
		double[][] the = new double[ny][nx];
		for(int iy=0; iy<ny; iy++){
		for(int ix=0; ix<nx; ix++){
			double xx = xMat[iy][ix];	double yy = yMat[iy][ix];
			the[iy][ix] = Math.atan2(yy,xx);
		}
		}
		return the;
	}

	public static double[][] AbsoluteValue( double[][] xMat, double[][] yMat){
		int ny = xMat.length;	int nx = xMat[0].length;
		double[][] abs = new double[ny][nx];
		for(int iy=0; iy<ny; iy++){
		for(int ix=0; ix<nx; ix++){
			double a = Math.pow(xMat[iy][ix],2.) + Math.pow(yMat[iy][ix],2.);
			a = Math.pow(a,0.5);
			abs[iy][ix] = RGBadjuster(a);
		}
		}
		return abs;
	}

	public static double Convolution(double f[][], double g[][]){
		int clm = g.length-1;
		int row = g[0].length-1;
		double a = 0.;

		for( int i=0; i<=row; i++){
		for( int j=0; j<=clm; j++){
			a += f[i][j]*g[i][j];
		}
		}
		return a;
	}

	public static double[][] Filter(double[][] mat8, double[][] k){
		int nw = mat8[0].length;
		int nh = mat8.length;
		double[][] res = new double[nh][nw];
		double[][] g = new double[3][3];
		double v;

		for(int iy=0; iy<nh; iy++){
		for(int ix=0; ix<nw; ix++){
			if( !(iy==0 || ix==0 || iy==nh-1 || ix==nw-1) ){
				for(int i=-1; i<=1; i++){
				for(int j=-1; j<=1; j++){
					g[i+1][j+1] = mat8[iy+i][ix+j];
				}
				}
				v = RGBadjuster( Convolution(k,g) );
			}else{
				v = 0.;
			}
			res[iy][ix] = v;
		}
		}
		return res;
	}

	public static void main(String args[]){
		String file_name = args[0];
		double[][] mat8,sbX,sbY,sbABS,sbAng;
		BufferedImage img;
		File input = new File(file_name);

		try{
			img = ImageIO.read( input );
		}catch(Exception e){
			System.out.println("Error! code:103");
			return;
		}
		System.out.printf( "size: %d x %d\n",img.getWidth(), img.getHeight() );

		mat8 = new double[img.getHeight()][img.getWidth()];

		mat8 = GrayScaling(img);
		try{
			ImageWriter("gray",mat8);
		}catch(Exception e){
			System.out.println("Error! code:104");
			System.out.println(e);
			return;
		}

// ------------ Gausian Filter ---------------
		try{
			double[][] k = { {1.,2.,1.},
			                 {2.,4.,2.},
			                 {1.,2.,1.} };
			for(int i=0;i<3;i++){
			for(int j=0;j<3;j++){
				k[i][j] = k[i][j]/16.;
			}
			}
			mat8 = Filter(mat8,k);

			ImageWriter("Gausian",mat8);
		}catch(Exception e){
			System.out.println("Error! code:105");
			return;
		}
// ------------ Gausian Filter ---------------

// ------------ Sobel Filter X ---------------
		try{
			double[][] k = { {-1.,0.,1.},
			                 {-2.,0.,2.},
			                 {-1.,0.,1.} };
			sbX = Filter(mat8,k);
			ImageWriter("sobelX",sbX);

		}catch(Exception e){
			System.out.println("Error! code:106");
			return;
		}
// ------------ Sobel Filter X ---------------


// ------------ Sobel Filter Y ---------------
		try{
			double[][] k = { {-1.,-2.,-1.},
			                 { 0., 0., 0.},
			                 { 1., 2., 1.} };
			sbY = Filter(mat8,k);
			ImageWriter("sobelY",sbY);

		}catch(Exception e){
			System.out.println("Error! code:107");
			return;
		}
// ------------ Sobel Filter Y ---------------

// ------------- Sobel Filter  ---------------
		sbABS = AbsoluteValue(sbX,sbY);
		try{
			ImageWriter("Sobel",sbABS);
		}catch(Exception e){
			System.out.println("Error! code:108");
			return;
		}
// ------------- Sobel Filter  ---------------

		sbAng = Angle(sbX,sbY);

// ----------- NonMaximumSuppression -------------
		mat8 = NonMaximumSuppression(sbABS,sbAng);
		try{
			ImageWriter("nms",mat8);
		}catch(Exception e){
			System.out.println("Error! code:109");
			return;
		}
// ----------- NonMaximumSuppression -------------

// ----------- HysteresisThreshold -------------
		mat8 = HysteresisThreshold(mat8,20.,10.);
		try{
			ImageWriter("ht",mat8);
		}catch(Exception e){
			System.out.println("Error! code:110");
			return;
		}
// ----------- HysteresisThreshold -------------
// hi!

		return;
	}


}