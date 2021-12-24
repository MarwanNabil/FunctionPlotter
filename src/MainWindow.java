import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.plaf.metal.MetalBorders.TextFieldBorder;

import com.mindfusion.charting.FunctionSeries;
import com.mindfusion.charting.GridType;
import com.mindfusion.charting.swing.LineChart;
import com.mindfusion.drawing.DashStyle;
import com.mindfusion.drawing.SolidBrush;
import static javax.swing.JOptionPane.showMessageDialog;

public class MainWindow extends JFrame {
	

	private static final long serialVersionUID = 1L;
	private static MainWindow me;
	private static JTextField func , domain;
	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					new MainWindow().setVisible(true);
				}
				catch (Exception exp)
				{
					
				}
			}
		});
	}
	
	protected MainWindow()
	{
		
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(720, 720);
		setTitle("Function Plotter");
		
		if(this.func == null)
			this.func = new JTextField("Type your equation here..");
		
		
		if(this.domain == null)
			this.domain = new JTextField("domain here... a b");
		
		
		try {
			getContentPane().add(func, BorderLayout.PAGE_START);
			getContentPane().add(domain, BorderLayout.PAGE_END);
		} catch(Exception E) {
			E.printStackTrace();
		}
		
		
		
		JButton drawBttn = new JButton("Draw");
		drawBttn.addActionListener((ActionListener) new ActionListener() {

		    @Override
		    public void actionPerformed(ActionEvent e) {
		    	
		    	String equation = new String(func.getText());
				String domainStr = new String(domain.getText());
				
				System.out.println(equation + " " + ((boolean) validEquation(equation)));
				System.out.println(domainStr + " " + ((boolean) validDomain(domainStr)) );
				
				if(!validEquation(equation) || !validDomain(domainStr)) {
					showMessageDialog(null, "Either your entered function or domain is wrong.");
					return;
				}
				
				equation = validateEquation(equation);
				System.out.println("Validated Equation : " + equation);
				String[] doma = domainStr.split(",");
				if(doma.length < 2) {
					doma = new String[]{"0" , "10"};
				}
				int minX = Integer.parseInt(doma[0]) , maxX = Integer.parseInt(doma[1]);
				System.out.println("Domain Min : " + minX + ", Domain Max : " + maxX);
				
				try {
					File f = new File("equation.txt");
					f.setWritable(true);
					f.createNewFile();
					FileWriter fw = new FileWriter(f);
					for(int i = 0; i < equation.length(); i++) {
						fw.append(equation.charAt(i));
					}
					fw.append('\n');
					fw.append(Integer.toString(minX));
					fw.append('\n');
					fw.append(Integer.toString(maxX));
					fw.close();
					f.setReadOnly();
				} catch (IOException e1) {
					//if already exits leave it as it is.
					e1.printStackTrace();
				}
				
				setVisible(false);
				main(null);
		    }
		});
		
		try {
			getContentPane().add(drawBttn , BorderLayout.LINE_END);
			getContentPane().add(initializeChart(), BorderLayout.CENTER);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private LineChart initializeChart()
	{
		
		String equation;
		int minX , maxX;
		LineChart lineChart = new LineChart();
		
		try {
			File f = new File("equation.txt");
			if(!f.exists()) {
				//if file doesn't exist , then do nothing..
				throw new FileNotFoundException();
			}
			Scanner sc = new Scanner(f);
			if(!sc.hasNext())
				return lineChart;
			equation = new String(sc.nextLine());
			if(!sc.hasNext())
				return lineChart;
			minX = sc.nextInt();
			if(!sc.hasNext())
				return lineChart;
			maxX = sc.nextInt();
			sc.close();
			
		} catch (FileNotFoundException e) {
			return lineChart;
		}
		
		try {
			System.out.println(equation);
			System.out.println("Min X is " + minX);
			System.out.println("Max X is " + maxX);
			
			
			lineChart.setSize(100 , 100);
			
			lineChart.getXAxis().setMinValue((double)minX);
			lineChart.getXAxis().setMaxValue((double)maxX);
			lineChart.getXAxis().setInterval(1.0);
			lineChart.getXAxis().setOrigin(0.0);
			lineChart.getXAxis().setTitle("X-axis");
			
			lineChart.getYAxis().setMaxValue(10.0);	
			lineChart.getYAxis().setTitle("Y-axis");
			
	
			//styling the grid
			lineChart.setGridType(GridType.Horizontal);
			lineChart.getTheme().setGridLineColor(new Color(192, 192, 192));
			lineChart.getTheme().setGridLineStyle(DashStyle.Dash);
			
			//setting the chart colors
			lineChart.getTheme().setHighlightStroke(new SolidBrush(new Color(255, 0, 61)));
	
			lineChart.getTheme().setCommonSeriesStrokes(
					Arrays.asList(
						new SolidBrush( new Color (136,44,180)),
						new SolidBrush( new Color (136,44,180))));
			lineChart.getTheme().setCommonSeriesFills(Arrays.asList(new SolidBrush( new Color (136,44,180 ))));
	
			
			FunctionSeries series1;
		
		
			//constructor take number of points to be displayed , starting x , ending x
			series1 = new FunctionSeries(equation, 1000, minX, maxX);
			series1.setTitle(equation + " " + "[" + Integer.toString(minX) + "," + Integer.toString(maxX) + "]");
			lineChart.getSeries().add(series1);
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			
		}
		return lineChart;
	}
	
	private boolean validEquation(String eq) {
		int sign = 0;
		ArrayList<String> allCof = new ArrayList<String>();
		StringBuilder curCof = new StringBuilder("");
		
		for(int i = 0; i < eq.length(); i++) {
			if(eq.charAt(i) == ' ')
				continue;
			if( isSign(eq.charAt(i)) ){
				allCof.add(curCof.toString());
				curCof = new StringBuilder("");
				sign++;
			} else {
				curCof.append(eq.charAt(i));
			}
		}
		if(curCof.length() > 0) {
			allCof.add(curCof.toString());
		}
		
		if(allCof.size() != sign + 1)
			return false;
		for(int i = 0; i < allCof.size(); i++) {
			//current cof. must be either x or number
			int dig = 0;
			for(int j = 0; j < allCof.get(i).length(); j++) {
				if( Character.isDigit(allCof.get(i).charAt(j)) )
					dig++;
			}
			if(allCof.get(i).toLowerCase().equals("x"))
				continue;
			//it's now must be digits
			//if it's not contains all digits then definetly it's a typo then return false;
			if(dig != allCof.get(i).length())
				return false;
				
		}
		
		return true;
	}
	
	private String validateEquation(String eq) {
		int sign = 0;
		ArrayList<String> allCof = new ArrayList<String>();
		StringBuilder curCof = new StringBuilder("");
		
		for(int i = 0; i < eq.length(); i++) {
			if(eq.charAt(i) == ' ')
				continue;
			if( isSign(eq.charAt(i)) ){
				allCof.add(curCof.toString());
				allCof.add(Character.toString(eq.charAt(i)));
				curCof = new StringBuilder("");
				sign++;
			} else {
				curCof.append(eq.charAt(i));
			}
		}
		if(curCof.length() > 0) {
			allCof.add(curCof.toString());
		}
		StringBuilder ret = new StringBuilder("");
		
		for(int i = 0; i < allCof.size(); i++) {
			if(isSign(allCof.get(i).charAt(0)) && allCof.get(i).equals("^") ) {
				int exp = Integer.parseInt(allCof.get(i + 1));  
				String base = allCof.get(i - 1);
				if(exp == 0) {
					ret.append("*1");
				} else {
					exp--;
					ret.append(base);
					while(exp-- > 0) {
						ret.append("*");
						ret.append(base);
					}
				}
				i++;
				continue;
			} else if(!(i < allCof.size()-1 && isSign(allCof.get(i+1).charAt(0)) && allCof.get(i+1).equals("^"))) {
				ret.append(allCof.get(i));
			} 
		}
		
		return ret.toString();
	}
	
	private boolean isSign(char x) {
		if(x == '+' || x == '-' || x == '*' || x == '/' || x == '^')
			return true;
		return false;
	}
	
	private boolean validDomain(String x){
		StringBuilder num = new StringBuilder("");
		int digCnt = 0;
		ArrayList<Integer> doma = new ArrayList<Integer>();
		int foundNumbers = 0;
		for(int i = 0; i < x.length(); i++) {
			if(x.charAt(i) == ' ')
				continue;
			if(x.charAt(i) == ',') {
				if(digCnt != num.length())
					return false;
				digCnt = 0;
				foundNumbers++;
				doma.add(Integer.parseInt(num.toString()));
				num = new StringBuilder("");
			} else {
				if(Character.isDigit(x.charAt(i)) || x.charAt(i) == '-') {
					digCnt++;
				}
				num.append(x.charAt(i));
			}
		}
		if(digCnt > 0) {
			foundNumbers++;
			doma.add(Integer.parseInt(num.toString()));
		}
		if(digCnt != num.length() || doma.size() != 2 || doma.get(0) > doma.get(0))
			return false;
		return true;
	}
	
}
