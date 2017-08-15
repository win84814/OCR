import java.util.*;
import java.io.File;
public class OCR{
	//static int templateRow;
	//static int templateCol;
	//static int searchRow;
	//static int searchCol;
	static int blackPixels;
	static int index;
	static ArrayList<Integer> searchRows = new ArrayList<Integer>();
	static ArrayList<Integer> searchCols = new ArrayList<Integer>();
	static HashMap<Character,Integer> templateRows = new HashMap<Character,Integer>();
	static HashMap<Character,Integer> templateCols = new HashMap<Character,Integer>();
	static HashMap<Character,int[][]> templateImages = new HashMap<Character,int[][]>();
	static HashMap<Character,Integer> templateImagePixels = new HashMap<Character,Integer>();
	static ArrayList<int[][]> searchImages = new ArrayList<int[][]>();
	static ArrayList<int[]> searchImagePixels = new ArrayList<int[]>();
	static ArrayList<Letter> outputMessage;
	static Scanner scan;
	public static void main(String args[]){

		try{
			//scan = new Scanner(new File("D:/input.txt"));
			scan = new Scanner(new File("D:/temp.txt"));
		}
		catch(Exception e){
			scan = new Scanner(System.in);
		}
		
		initializeTemplateImages();
		initializeSearchImages();
		start();
	}
	static void initializeTemplateImages(){
		int N = scan.nextInt(); // number of template images
		int templateRow = scan.nextInt(); // input rows and cols
		int templateCol = scan.nextInt();
		for(int i = 0 ; i < N ; i++){ // a letter , an image , a '@'
			char ch = scan.next().charAt(0); // input a letter
			int[][] image = new int[templateRow][templateCol];
			//int pixel = 0;
			for(int j = 0 ; j < templateRow ; j++){ // input an image
				String inputImageRow = scan.next();
				for(int k = 0 ; k < templateCol ; k++){ // add 0 or 1 into image
					image[j][k] = (inputImageRow.charAt(k)=='*')?1:0; //  if '*' then value = 1 ,else value = 0.
					//if(image[j][k] == 1) pixel++;
				}
			}
			image = templateImageReduceFrame(image,ch);
			templateImages.put(ch,image);
			
			scan.next(); // end of '@'
		}
		//System.out.println("End initializeTemplateImages");
	}
	static void initializeSearchImages(){
		int S = scan.nextInt(); // number of search images
		for(int i = 0 ; i < S ; i++){
			int searchRow = scan.nextInt();
			int searchCol = scan.nextInt();
			int[] pixels = {0};
			searchRows.add(searchRow);
			searchCols.add(searchCol);
			int[][] image = new int[searchRow][searchCol];
			for(int j = 0 ; j < searchRow ; j++){ // input an image
				String inputImageRow = scan.next();
				for(int k = 0 ; k < searchCol ; k++){ // add 0 or 1 into image
					image[j][k] = (inputImageRow.charAt(k)=='*')?1:0; //  if '*' then value = 1 ,else value = 0.
					if(image[j][k] == 1) pixels[0]++;
				}
			}
			searchImagePixels.add(pixels);
			searchImages.add(image);
			scan.next(); // end of '@'
		}
	}
	static void initializeCase(int index){
		outputMessage = new ArrayList<Letter>();
		blackPixels = Integer.MAX_VALUE;
		OCR.index = index;
	}
	static void start(){
		for(int i = 0 ; i < searchImages.size() ; i++){
			if(i!=0) System.out.println(); // if searchImages > 1 , then newline
			initializeCase(i);
			DP(templateImages,new ArrayList<Letter>(),searchImages.get(index),0,searchImagePixels.get(index)); // starts DP
			printLetters(outputMessage);
		}
	}
	static void DP(HashMap<Character,int[][]> templateImages,ArrayList<Letter> message,int[][] searchImage,int depth,int[] pixels){
		if(templateImages.size()==0){
			if(pixels[0]<blackPixels){
				blackPixels = pixels[0];
				outputMessage = message;
			}
		}
		else{
			for(Iterator<Character> it = templateImages.keySet().iterator();it.hasNext();){
				char ch = it.next();
				int[][] image = templateImages.get(ch);
				
				HashMap<Character,int[][]> copyTemplateImages = (HashMap<Character,int[][]>)templateImages.clone(); // clone the templateImages
				ArrayList<Letter> copyMessage = (ArrayList<Letter>)message.clone(); // clone the message
				int[][] copySearchImage = new int[searchImage.length][];  // clone the searchImage
				for(int i=0;i<searchImage.length;i++)
					copySearchImage[i] = searchImage[i].clone();
				copyTemplateImages.remove(ch); //  remove the char
				int[] copyPixels = {pixels[0]};
				
				ArrayList<Letter> makeResult = templateMatching(ch,image,copySearchImage,copyPixels); // get the result of templateMatching
				copyMessage.addAll(makeResult); // add result into message
				DP(copyTemplateImages,copyMessage,copySearchImage,depth+1,copyPixels); // repeat DP until  templateImages.size()==0
			}
		}
	}
	static ArrayList<Letter> templateMatching(char templateChar,int[][] templateImage,int[][] searchImage,int[] pixels){
		ArrayList<Letter> result = new ArrayList<Letter>();
		int templateRow = templateRows.get(templateChar);
		int templateCol = templateCols.get(templateChar);
		if(pixels[0] < templateImagePixels.get(templateChar)) return result; // save time
		for(int i = 0; i+templateRow <= searchRows.get(index); i++){ //match each positions
			for(int j = 0 ; j+templateCol <= searchCols.get(index) ; j++){
				boolean complete = true;
				for(int k = i ; k < i+templateRow; k++){ // match each pixel of images
					for(int l = j ; l < j+templateCol ; l++){
						if(templateImage[k-i][l-j] != 1) continue; // only match the black part
						if(templateImage[k-i][l-j] != searchImage[k][l]){ 
							complete = false;
							break;
						}
					}
					if(!complete) break;
				}
				if(complete) {
					pixels[0] -= templateImagePixels.get(templateChar);
					for(int k = i ; k < i+templateRow; k++){ // match each pixel of images
						for(int l = j ; l < j+templateCol ; l++){
							if(templateImage[k-i][l-j] != 1) continue; // only match the black part
							if(templateImage[k-i][l-j] == searchImage[k][l]){ 
								searchImage[k][l] = 0; // set 1 to 0
							}
						}
					}
					result.add(new Letter(i,j,templateChar));
				}
			}
		}
		return result;
	}
	static void printLetters(ArrayList<Letter> inputLetters){
		if(inputLetters.size()==0){
			return ;
		}
		Collections.sort(inputLetters,new Comparator<Letter>(){
			@Override
			public int compare(Letter letter1,Letter letter2){
				return letter1.startCol - letter2.startCol;
			}
		});
		for(Iterator<Letter> it = inputLetters.iterator(); it.hasNext();){
			Letter letter = it.next();
			System.out.print(letter.character);
		}
	}
	static int getBlackPixels(int[][] image){
		int result = 0;
		for(int i = 0 ; i <image.length;i++){
			for(int j = 0 ; j <image[i].length;j++){
				if(image[i][j] == 1) result++;
			}
		}
		return result;
	}
	static void showImage(int[][] image){ // test
		System.out.println();
		for(int i = 0 ; i < image.length ; i++){
			for(int j = 0 ; j < image[i].length ; j++){
				System.out.print(image[i][j]);
			}
			System.out.println();
		}
	}
	static int[][] templateImageReduceFrame(int[][] image,char ch){
		int minRow = Integer.MAX_VALUE , maxRow = Integer.MIN_VALUE , minCol = Integer.MAX_VALUE , maxCol = Integer.MIN_VALUE;
		int pixel = 0;
		for(int i = 0 ; i < image.length ; i++){
			for(int j = 0 ; j < image[i].length ; j++){
				if(image[i][j] == 1){
					pixel ++;
					if(i < minRow) minRow = i;
					if(i > maxRow) maxRow = i;
					if(j < minCol) minCol = j;
					if(j > maxCol) maxCol = j;
					
				}
			}
		}
		
		int row = maxRow - minRow + 1 , col = maxCol - minCol + 1;
		int[][] newImage = new int[row][col];
		for(int i = minRow ; i <= maxRow ; i++){
			for(int j = minCol ; j <= maxCol ; j++){
				newImage[i - minRow][j - minCol] = image[i][j];
			}
		}
		//image = newImage;
		templateImagePixels.put(ch,pixel);
		templateRows.put(ch, row);
		templateCols.put(ch, col);
		return newImage;
	}
}
class Letter{
	int startRow;
	int startCol;
	char character;
	Letter(int startRow,int startCol,char character){
		this.startRow = startRow;
		this.startCol = startCol;
		this.character = character;
	}
}