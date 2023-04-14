package lapjv;

import java.util.Arrays;

public class MatrixUtils
{
	public static int[][] makeSquare(int[][] matrix, int fillValue)
	{
		int rdim = matrix.length;
		int cdim = matrix[0].length;
		
		if (rdim==cdim)
		{
			return transpose(matrix);
		}
		else if (rdim>cdim)
		{
			matrix = transpose(matrix);
		    int[][] temp = new int[rdim-cdim][rdim];
		    for (int[] row: temp) Arrays.fill(row,fillValue);
		    matrix = append(matrix,temp);
		    return transpose(matrix);
		}
		else
		{
			int[][] temp = new int[cdim-rdim][cdim];
			for (int[] row: temp) Arrays.fill(row,fillValue);
		    return append(matrix,temp);
		}
	}
	
	public static int[][] transpose(int[][] matrix)
	{
		int m = matrix.length;
		int n = matrix[0].length;
		
		int[][] transposed = new int[n][m];
		
		for(int i = 0; i < m; i++) 
		{
		  for(int j = 0; j < n; j++) 
		  {
		    transposed[j][i] = matrix[i][j];
		  }
		}
		return transposed;
	}
	
	public static int sum(int[][] matrix)
	{
		int sum=0;
		
		int m = matrix.length;
		int n = matrix[0].length;
		
		for(int i = 0; i < m; i++) 
		{
		  for(int j = 0; j < n; j++) 
		  {
		    sum+=matrix[i][j];
		  }
		}
		return sum;
	}
	
	public static int[][] append(int[][] a, int[][] b)
	{
        int[][] result = new int[a.length + b.length][];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

}
