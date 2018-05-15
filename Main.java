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
        		System.out.println("\n��1��ʼ��������,��2��ʼð������");
        		inputChoice = new Scanner(System.in).nextInt();
        		if (inputChoice == 1) {
        			doQuickSort();
        			System.out.println("\n���������ʼ��������");
        			anyKey = new Scanner(System.in).next();		
        			doBubbleSort();
        			break;
        		} else if (inputChoice == 2){
        			doBubbleSort();
        			System.out.println("\n���������ʼ��������");
        			anyKey = new Scanner(System.in).next();	
        			doQuickSort();
        			break;
        		} else {
        			
        		}
        	} catch (Exception e){
        		System.out.println("\n������1��2");
        	}
        } while (true);
        
		
		
		System.out.println("����������ʱ��" + sortExcTime + "ms");
		System.out.println("ð��������ʱ��" + bubbleExcTime + "ms");
    } 
    
    
    
    /**
     * ��쳲���������������򣬴�ӡ�������������������к�ð������
     */
    private static void randomFibnoacci() {
    	//��ʼ���� 
		System.out.println("\n���������ʼ����˳��");
		anyKey = new Scanner(System.in).next();
		list = (List) Arrays.asList(integerThousand);
		Collections.shuffle(list);
		
		// ���ҳɹ�
		for (int i = 0; i < n; i++) {
			System.out.println(list.get(i));
		}
		quickList.addAll(list);
		bubbleList.addAll(list);
	}

	
    /**  
     * ����쳲���������
     */
    private static void createFibnoacci() {
    	for (int i = 0; i < n; i++) {
			integerThousand[i] = fibonacciNormal(i + 1);
			System.out.println((i + 1) + "   " + integerThousand[i]);
		}
	}

    /**
     * ��ʼ������
     */
	private static void initData() {
    	quickList = new ArrayList();
    	bubbleList = new ArrayList();
    	do {
    		try {
    			System.out.println("\n��������Ҫ�õ���쳲�����������");
    			n = new Scanner(System.in).nextInt();
    			integerThousand = new BigInteger[n];
    			break;
    		} catch (Exception e){
    			System.out.println("\n����������");
    		}
    	} while (true);
	}

	/**
	 * ִ��ð������ ����ӡ������ʱ��
	 */
	public static void doBubbleSort() {
		long startTime=System.currentTimeMillis();
		bubbleSort();
		long endTime=System.currentTimeMillis();
		bubbleExcTime=(float)(endTime-startTime); 
		for (int i = 0; i < n; i++) {
			System.out.println(bubbleList.get(i));
		}
		System.out.println("\nִ��ʱ�䣺"+bubbleExcTime+"ms"); 
	}
    
	/**
	 * ִ�п������򣬲���ӡ������ʱ��
	 */
	public static void doQuickSort() {
		long startTime=System.currentTimeMillis();
		quicksort(0, n - 1);
		for (int i = 0; i < n; i++) {
			System.out.println(quickList.get(i));
		}
		long endTime=System.currentTimeMillis();
		sortExcTime=(float)(endTime-startTime); 
		System.out.println("\nִ��ʱ�䣺"+sortExcTime+"ms"); 
	}
	
    /**
     * ���ַ���ת��Ϊ�������ʽ�洢
     * int 2^31-1  long 2^63-1 Integer
     * @param n 쳲��������ĸ���
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
     * ��������
     * @param left ��Ҫ��������ĵ�һλ
     * @param right ��Ҫ������������һλ
     */
    public static void quicksort(int left,int right){
        BigInteger temp;
        int i,j;
        if(left > right)
           return;
        temp = (BigInteger) quickList.get(left); //temp�д�ľ��ǻ�׼��
        i=left;
        j=right;
        while(i!=j){
               //˳�����Ҫ��Ҫ�ȴ��ұ߿�ʼ��
               while((((BigInteger)quickList.get(j)).compareTo(temp) >=0) && i<j)
                        j--;
               //�����ұߵ�
               while((((BigInteger)quickList.get(i)).compareTo(temp) <=0) && i<j)
                        i++;
               //�����������������е�λ��
               if(i<j) {
            	   Collections.swap(quickList, i, j);
               } 
        }
        //���ս���׼����λ
        Collections.swap(quickList, left, i);
        
        quicksort(left,i-1);//����������ߵģ�������һ���ݹ�Ĺ���
        quicksort(i+1,right);//���������ұߵ� ��������һ���ݹ�Ĺ���
    }

    /**
     * ð������
     * ִ��ð������
     */
    public static void bubbleSort() {
        for(int i =0;i<bubbleList.size() - 1;i++) {
            for(int j=0;j<bubbleList.size() - i - 1;j++) {//-1Ϊ�˷�ֹ���
                if(((BigInteger)bubbleList.get(j)).compareTo((BigInteger)bubbleList.get(j+1)) > 0) {
                    Collections.swap(bubbleList, j, j + 1);
                }
            }    
        }
    }
}  