import java.util.*;
import java.io.File;
public class OCR{
	static int blackPixels;
	static int index;
	static final double threshold = 0.90;
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
			scan = new Scanner(new File("D:/temp2.txt"));
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
		for(int i = 0 ; i < N ; i++){
			char ch = scan.next().charAt(0); // input a letter
			int templateRow = scan.nextInt(); // input rows and cols
			int templateCol = scan.nextInt();
			int[][] image = new int[templateRow][templateCol];
			for(int j = 0 ; j < templateRow ; j++){ // input an image
				String inputImageRow = scan.next();
				for(int k = 0 ; k < templateCol ; k++){ 
					image[j][k] = (inputImageRow.charAt(k)=='*')?1:0; //  if '*' then value = 1 ,else value = 0.
				}
			}
			image = templateImageReduceFrame(image,ch);
			templateImages.put(ch,image);
		}
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
				for(int k = 0 ; k < searchCol ; k++){ 
					image[j][k] = (inputImageRow.charAt(k)=='*')?1:0; //  if '*' then value = 1 ,else value = 0.
					if(image[j][k] == 1) pixels[0]++;
				}
			}
			searchImagePixels.add(pixels);
			searchImages.add(image);
		}
	}
	static void initializeCase(int index){
		outputMessage = new ArrayList<Letter>();
		blackPixels = Integer.MAX_VALUE;
		OCR.index = index;
	}
	static void start(){
		for(int i = 0 ; i < searchImages.size() ; i++){
			if(i!=0) System.out.println();
			initializeCase(i);
			ArrayList<Letter> letters = templateMatching(searchImages.get(index));
			DP(letters,new ArrayList<Letter>(),searchImages.get(index),searchImagePixels.get(index));
			printLetters(outputMessage);
		}
	}
	static void DP(ArrayList<Letter> letters,ArrayList<Letter> message,int[][] searchImage,int[] pixels){
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
			boolean removeSuccess = pixelsRemove(letter,copySearchImage);
			if(removeSuccess){
				copyMessage.add(letter);
				copyPixels[0] -= templateImagePixels.get(letter.character);
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
						result.add(new Letter(i,j,templateChar));
					}
				}
			}
		}
		return result;
	}
	static boolean pixelsRemove(Letter letter , int[][] searchImage){
		char templateChar = letter.character;
		int[][] templateImage = templateImages.get(templateChar);
		int templateRow = templateRows.get(templateChar);
		int templateCol = templateCols.get(templateChar);
		int startRow = letter.startRow;
		int startCol = letter.startCol;
		double erased = 0.0;
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
		double accuracyRate = erased/total;
		return accuracyRate > threshold;
	}
	static void printLetters(ArrayList<Letter> letters){
		if(letters.size()==0){
			return ;
		}
		Collections.sort(letters,new Comparator<Letter>(){
			@Override
			public int compare(Letter letter1,Letter letter2){
				return letter1.startCol - letter2.startCol;
			}
		});
		for(Iterator<Letter> it = letters.iterator(); it.hasNext();){
			Letter letter = it.next();
			System.out.print(letter.character);
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