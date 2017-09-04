import java.util.*;
import java.io.File;
import java.io.FileWriter;
public class OCR{
	static int blackPixels;
	static int index;
	static final int FAIL = -1;
	static final double TMthreshold = 0.94;
	static final double EraseThreshold = 0.90;
	static ArrayList<Integer> searchRows = new ArrayList<Integer>();
	static ArrayList<Integer> searchCols = new ArrayList<Integer>();
	static HashMap<Character,Integer> templateRows = new HashMap<Character,Integer>();
	static HashMap<Character,Integer> templateCols = new HashMap<Character,Integer>();
	static HashMap<Character,int[][]> templateImages = new HashMap<Character,int[][]>();
	static HashMap<Character,Integer> templateImagePixels = new HashMap<Character,Integer>();
	static ArrayList<int[][]> searchImages = new ArrayList<int[][]>();
	static ArrayList<int[]> searchImagePixels = new ArrayList<int[]>();
	static ArrayList<Letter> outputMessage;
	static HashSet<String> DPnode;
	static Scanner scan;
	static long startTime;
	public static void main(String args[]){
		startTime = System.currentTimeMillis();
		try{
			scan = new Scanner(new File("D:/pgin.txt"));
		}
		catch(Exception e){
			scan = new Scanner(System.in);
		}
			
		initializeTemplateImages();
		initializeSearchImages();
		start();
		System.out.println("h7h1IF0LUMp <- Correct");
		System.out.println("Using Time:" + (System.currentTimeMillis() - startTime) + " ms");
	}
	static void initializeTemplateImages(){
		int N = scan.nextInt(); // number of template images
		for(int i = 0 ; i < N ; i++){
			char ch = scan.next().charAt(0); // input a letter
			int templateRow = scan.nextInt(); // input rows and cols
			int templateCol = scan.nextInt();
			int[][] image = new int[templateRow][templateCol];
			int pixels = 0;
			for(int j = 0 ; j < templateRow ; j++){ // input an image
				String inputImageRow = scan.next();
				for(int k = 0 ; k < templateCol ; k++){ 
					image[j][k] = (inputImageRow.charAt(k)=='*')?1:0; //  if '*' then value = 1 ,else value = 0.
					if(image[j][k] == 1) pixels++;
				}
			}
			image = templateImageReduceFrame(image,ch);
			templateImagePixels.put(ch,pixels);
			templateImages.put(ch,image);
		}
	}
	static void initializeSearchImages(){
		int S = scan.nextInt(); // number of search images
		for(int i = 0 ; i < S ; i++){
			int searchRow = scan.nextInt();
			int searchCol = scan.nextInt();
			int[] pixels = {0};
			int[][] image = new int[searchRow][searchCol];
			for(int j = 0 ; j < searchRow ; j++){ // input an image
				String inputImageRow = scan.next();
				for(int k = 0 ; k < searchCol ; k++){ 
					image[j][k] = (inputImageRow.charAt(k)=='*')?1:0; //  if '*' then value = 1 ,else value = 0.
					if(image[j][k] == 1) pixels[0]++;
				}
			}
			image = searchImageReduceFrame(image);
			searchImagePixels.add(pixels);
			searchImages.add(image);
		}
	}
	static void initializeCase(int index){
		outputMessage = new ArrayList<Letter>();
		DPnode = new HashSet<String>();
		blackPixels = Integer.MAX_VALUE;
		OCR.index = index;
	}
	static void start(){
		for(int i = 0 ; i < searchImages.size() ; i++){
			initializeCase(i);
			ArrayList<Letter> letters = templateMatching(searchImages.get(index));
			System.out.println("Passed template images : " + letters.size());
			DP(letters,new ArrayList<Letter>(),searchImages.get(index),searchImagePixels.get(index));
			System.out.println(lettersToString(outputMessage));
		}
	}
	static void DP(ArrayList<Letter> letters,ArrayList<Letter> message,int[][] searchImage,int[] pixels){
		String check = lettersToString(message);
		if(DPnode.contains(check)){
			return ;
		}
		else{
			DPnode.add(check);
		}
		if(pixels[0]<blackPixels){
			blackPixels = pixels[0];
			outputMessage = message;
		}
		if(letters.size()==0){
			return ;
		}
		for(int i = 0 ; i < letters.size() ; i++){   
			Letter letter = letters.get(i);
			ArrayList<Letter> copyLetters = (ArrayList<Letter>)letters.clone(); 
			ArrayList<Letter> copyMessage = (ArrayList<Letter>)message.clone(); 
			int[][] copySearchImage = new int[searchImage.length][];  
			for(int j=0;j<searchImage.length;j++)
				copySearchImage[j] = searchImage[j].clone();
			copyLetters.remove(i);
			int[] copyPixels = {pixels[0]};
			int removedPixels = removePixels(letter,copySearchImage);
			if(removedPixels != FAIL){
				copyMessage.add(letter);
				//
				Collections.sort(copyMessage,new Comparator<Letter>(){
					@Override
					public int compare(Letter letter1,Letter letter2){
						return letter1.startCol - letter2.startCol;
					}
				});
				//
				copyPixels[0] -= removedPixels;
				DP(copyLetters,copyMessage,copySearchImage,copyPixels); 
			}
		}
	}
	static ArrayList<Letter> templateMatching(int[][] searchImage){
		ArrayList<Letter> result = new ArrayList<Letter>();
		for(Iterator<Character> it = templateImages.keySet().iterator();it.hasNext();){
			char templateChar = it.next();
			int[][] templateImage = templateImages.get(templateChar);
			int templateRow = templateRows.get(templateChar);
			int templateCol = templateCols.get(templateChar);
			int searchRow = searchRows.get(index);
			int searchCol = searchCols.get(index);
			
			for(int i = 0; i+templateRow <= searchRow; i++){ //match each positions
				for(int j = 0 ; j+templateCol <=  searchCol; j++){
					double SAD = 0;
					double templateArea = templateRow * templateCol;
					for(int k = i ; k < i+templateRow; k++){ // match each pixel of both images
						for(int l = j ; l < j+templateCol ; l++){
							int pixelT = templateImage[k-i][l-j];
							int pixelS = searchImage[k][l];
							SAD += Math.abs(pixelT - pixelS);
						}
					}
					if(1 - SAD / templateArea > TMthreshold){
						result.add(new Letter(i,j,templateChar));
					}
				}
			}
		}
		return result;
	}
	static int removePixels(Letter letter , int[][] searchImage){
		char templateChar = letter.character;
		int[][] templateImage = templateImages.get(templateChar);
		int templateRow = templateRows.get(templateChar);
		int templateCol = templateCols.get(templateChar);
		int startRow = letter.startRow;
		int startCol = letter.startCol;
		int erased = 0;
		int total = templateImagePixels.get(templateChar);
		
		for(int i = startRow; i < startRow+templateRow; i++){       
			for(int j = startCol ; j < startCol+templateCol ; j++){
				if(templateImage[i - startRow][j - startCol] != 1) continue; 
				if(templateImage[i - startRow][j - startCol] == searchImage[i][j]){ 
						searchImage[i][j] = 0; 
						erased ++;
				}
			}
		}
		//double accuracyRate = (templateRow * templateCol -(total - erased)) / templateRow * templateCol ;
		double accuracyRate = erased*1.0/total;
		if(accuracyRate > EraseThreshold) return erased;
		else return FAIL;
	}
	static int[][] templateImageReduceFrame(int[][] image,char ch){
		int minRow = Integer.MAX_VALUE , maxRow = Integer.MIN_VALUE , minCol = Integer.MAX_VALUE , maxCol = Integer.MIN_VALUE;
		for(int i = 0 ; i < image.length ; i++){
			for(int j = 0 ; j < image[i].length ; j++){
				if(image[i][j] == 1){
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
		templateRows.put(ch, row);
		templateCols.put(ch, col);
		return newImage;
	}
	static int[][] searchImageReduceFrame(int[][] image){
		int minRow = Integer.MAX_VALUE , maxRow = Integer.MIN_VALUE , minCol = Integer.MAX_VALUE , maxCol = Integer.MIN_VALUE;
		for(int i = 0 ; i < image.length ; i++){
			for(int j = 0 ; j < image[i].length ; j++){
				if(image[i][j] == 1){
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
		searchRows.add(row);
		searchCols.add(col);
		return newImage;
	}
	static String lettersToString(ArrayList<Letter> letters){
		StringBuffer sb = new StringBuffer();
		for(int i = 0 ; i < letters.size() ; i++)
			sb.append(letters.get(i).character);
		return sb.toString();
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