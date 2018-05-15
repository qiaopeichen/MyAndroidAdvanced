import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;   
public class Main {
	
	public static List list;
	public static List quickList;
	public static List bubbleList;
	public static BigInteger integerThousand[];
	public static int n;
	public static int inputChoice;
	private static float bubbleExcTime;
	private static float sortExcTime;
	private static String anyKey;
	
    public static void main(String[] args) {  
    	initData();
    	
        createFibnoacci();
		
        randomFibnoacci();
		
        do {
        	try {
        		System.out.println("\n按1开始快速排序,按2开始冒泡排序");
        		inputChoice = new Scanner(System.in).nextInt();
        		if (inputChoice == 1) {
        			doQuickSort();
        			System.out.println("\n按任意键开始快速排序");
        			anyKey = new Scanner(System.in).next();		
        			doBubbleSort();
        			break;
        		} else if (inputChoice == 2){
        			doBubbleSort();
        			System.out.println("\n按任意键开始快速排序");
        			anyKey = new Scanner(System.in).next();	
        			doQuickSort();
        			break;
        		} else {
        			
        		}
        	} catch (Exception e){
        		System.out.println("\n请输入1或2");
        	}
        } while (true);
        
		
		
		System.out.println("快速排序用时：" + sortExcTime + "ms");
		System.out.println("冒泡排序用时：" + bubbleExcTime + "ms");
    } 
    
    
    
    /**
     * 将斐波那契数列随机排序，打印出来，并拷贝快排数列和冒泡数列
     */
    private static void randomFibnoacci() {
    	//开始排序 
		System.out.println("\n按任意键开始打乱顺序");
		anyKey = new Scanner(System.in).next();
		list = (List) Arrays.asList(integerThousand);
		Collections.shuffle(list);
		
		// 打乱成功
		for (int i = 0; i < n; i++) {
			System.out.println(list.get(i));
		}
		quickList.addAll(list);
		bubbleList.addAll(list);
	}

	
    /**  
     * 生成斐波那契数列
     */
    private static void createFibnoacci() {
    	for (int i = 0; i < n; i++) {
			integerThousand[i] = fibonacciNormal(i + 1);
			System.out.println((i + 1) + "   " + integerThousand[i]);
		}
	}

    /**
     * 初始化数据
     */
	private static void initData() {
    	quickList = new ArrayList();
    	bubbleList = new ArrayList();
    	do {
    		try {
    			System.out.println("\n请输入您要得到的斐波那契个数：");
    			n = new Scanner(System.in).nextInt();
    			integerThousand = new BigInteger[n];
    			break;
    		} catch (Exception e){
    			System.out.println("\n请输入数字");
    		}
    	} while (true);
	}

	/**
	 * 执行冒泡排序 并打印出排序时间
	 */
	public static void doBubbleSort() {
		long startTime=System.currentTimeMillis();
		bubbleSort();
		long endTime=System.currentTimeMillis();
		bubbleExcTime=(float)(endTime-startTime); 
		for (int i = 0; i < n; i++) {
			System.out.println(bubbleList.get(i));
		}
		System.out.println("\n执行时间："+bubbleExcTime+"ms"); 
	}
    
	/**
	 * 执行快速排序，并打印出排序时间
	 */
	public static void doQuickSort() {
		long startTime=System.currentTimeMillis();
		quicksort(0, n - 1);
		for (int i = 0; i < n; i++) {
			System.out.println(quickList.get(i));
		}
		long endTime=System.currentTimeMillis();
		sortExcTime=(float)(endTime-startTime); 
		System.out.println("\n执行时间："+sortExcTime+"ms"); 
	}
	
    /**
     * 将字符串转化为数组的形式存储
     * int 2^31-1  long 2^63-1 Integer
     * @param n 斐波那契数的个数
     * @return 
     */
    public static BigInteger fibonacciNormal(int n){  
		if (n == 1) {
			return new BigInteger("0");
		}
		BigInteger n1 = new BigInteger("0"), n2 = new BigInteger("1"), sn = new BigInteger("0");
		for (int i = 0; i < n - 1; i++) {
			sn = n1.add(n2);
			n1 = n2;
			n2 = sn;
		}
		return sn;
    }
    
    /**
     * 快速排序法
     * @param left 需要排序的数的第一位
     * @param right 需要排序的数的最后一位
     */
    public static void quicksort(int left,int right){
        BigInteger temp;
        int i,j;
        if(left > right)
           return;
        temp = (BigInteger) quickList.get(left); //temp中存的就是基准数
        i=left;
        j=right;
        while(i!=j){
               //顺序很重要，要先从右边开始找
               while((((BigInteger)quickList.get(j)).compareTo(temp) >=0) && i<j)
                        j--;
               //再找右边的
               while((((BigInteger)quickList.get(i)).compareTo(temp) <=0) && i<j)
                        i++;
               //交换两个数在数组中的位置
               if(i<j) {
            	   Collections.swap(quickList, i, j);
               } 
        }
        //最终将基准数归位
        Collections.swap(quickList, left, i);
        
        quicksort(left,i-1);//继续处理左边的，这里是一个递归的过程
        quicksort(i+1,right);//继续处理右边的 ，这里是一个递归的过程
    }

    /**
     * 冒泡排序法
     * 执行冒泡排序
     */
    public static void bubbleSort() {
        for(int i =0;i<bubbleList.size() - 1;i++) {
            for(int j=0;j<bubbleList.size() - i - 1;j++) {//-1为了防止溢出
                if(((BigInteger)bubbleList.get(j)).compareTo((BigInteger)bubbleList.get(j+1)) > 0) {
                    Collections.swap(bubbleList, j, j + 1);
                }
            }    
        }
    }
}  