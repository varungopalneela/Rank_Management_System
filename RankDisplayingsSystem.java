import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.sql.*;
class Student{
    double grade;
    String stdid;
    Student(double grade,String stdid){
        this.grade=grade;
        this.stdid=stdid;
    }

    void displayResult(ArrayList<Student> students,Statement stmt1,JTextArea outputTextArea){
        Collections.sort(students, new Comparator<Student>() {
            public int compare(Student s1, Student s2) {
                double s1Grade = s1.grade;
                double s2Grade = s2.grade;
                if (s1Grade > s2Grade) {
                    return -1;
                } else if (s1Grade < s2Grade) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
        outputTextArea.append("\tRanking\tStudentID\tName\tOverall Grade\n");
        try{
            for (int i = 0; i < students.size(); i++) {
            String studentid = students.get(i).stdid;
           // System.out.println(studentid);
            ResultSet rs6=stmt1.executeQuery("select * from course where stdId='"+studentid+"'");
            rs6.next();
            outputTextArea.append("\t"+ (i+1) + "\t" +rs6.getString("stdId")+"\t"+rs6.getString("stdName")+"\t" + rs6.getDouble("Grade")+"\n");
           }
        }
        catch(SQLException e){System.out.println(e);}
        
    }
}

public class RankDisplayingsSystem extends JFrame {
    private final JTextArea outputTextArea;
    private final JTextField studentNameField;
    private final JComboBox<String> subjectComboBox;
    private final JTextField marksField;
    private final JTextField studentId;
    private final JTextField subjectMaxMarks;
    ArrayList<Student> stds;
    Statement stmt1;

    public RankDisplayingsSystem(Statement stmt1) {
        this.stmt1=stmt1;
        // Initialize GUI components
        outputTextArea = new JTextArea(15, 30);
        outputTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputTextArea);
        scrollPane.setFont(new java.awt.Font("Courier New", 0, 20));

        studentNameField = new JTextField(10);
        subjectComboBox = new JComboBox<>();
        marksField = new JTextField(5);
        studentId=new JTextField(10);
        subjectMaxMarks=new JTextField(5);

        JButton addStudentButton = new JButton("Add Student");
        JButton addSubjectButton = new JButton("Add Subject");
        JButton addMarksButton = new JButton("Add Marks");
        JButton displayRankButton = new JButton("Display Rank");
        JButton displayStudents=new JButton("Display students");
        JButton Home=new JButton("Home");

        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel inputPanel = new JPanel(new FlowLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout());

        // Add subjects to the combo box
      //  subjectComboBox.addItem(""); // Blank option
        try{
            ResultSet rs10=stmt1.executeQuery("select * from subjects");
            while(rs10.next()){ 
                subjectComboBox.addItem(rs10.getString("subName"));
            }
        }   
        catch(SQLException e){System.out.println(e);}


        subjectComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedSubject = (String) subjectComboBox.getSelectedItem();
                if (selectedSubject != null && !selectedSubject.isEmpty()) {
                    marksField.setEnabled(true);
                } else {
                    marksField.setEnabled(false);
                }
            }
        });

        // Add action listeners to buttons
        addStudentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e1) {
                try{
                String studentName = studentNameField.getText();
                String studentid=studentId.getText();
                ResultSet rs13=stmt1.executeQuery("select * from course where stdId='"+studentid+"'");
                if(rs13.next()){outputTextArea.append("studentId already exists\n");}// ID already exist}
                else{
                if (!studentName.isEmpty() && !studentid.isEmpty()) {
                    try{
                    ResultSet rs16=stmt1.executeQuery("SELECT count(*) FROM information_schema.columns WHERE table_name ='course';");
                    rs16.next();
                    int count = rs16.getInt(1)-2;
                    //System.out.println(count);
                    String s="";
                    while(count>0){
                        s+=",null";
                        count-=1;
                    }
                    stmt1.executeUpdate("insert into course values('"+studentName+"','"+studentid+"'"+s+")");
                    }
	                catch(SQLException e){System.out.println(e);}
                    studentNameField.setText("");
                    //studentId.setText("");
                    outputTextArea.append("Added student: " + studentName + " with Id "+studentid+" \n");
                }
                else{outputTextArea.append("Enter valid input\n");}//entervalidinput
                }
            }
            catch(SQLException e){System.out.println(e);} 
            }
        });

        addSubjectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e1) {
                try{
                String newSubject = JOptionPane.showInputDialog("Enter subject name:");
                try{
                int subjectmaxmarks=Integer.parseInt(subjectMaxMarks.getText());
                ResultSet rs11=stmt1.executeQuery("select * from subjects where subName='"+newSubject+"'");
                if(rs11.next()){outputTextArea.append("Subject already exists\n");}//subject already exists}
                else if(subjectmaxmarks<0){outputTextArea.append("enter valid total marks");}
                else{
                if (newSubject != null && !newSubject.isEmpty()) {
                    try{stmt1.executeUpdate("insert into subjects values('"+newSubject+"',"+subjectmaxmarks+")");
                    stmt1.executeUpdate("ALTER TABLE course ADD COLUMN "+newSubject+" int");}
	                catch(SQLException e){System.out.println(e);}
                    subjectComboBox.addItem(newSubject);
                    //subjectMaxMarks.setText("");
                    outputTextArea.append("Added subject: " + newSubject + "\n");
                }
            }}
            catch(NumberFormatException e5){outputTextArea.append("enter valid total marks\n");}}
            catch(SQLException e){System.out.println(e);} 
            }
        });

        displayStudents.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e1) {
                try{
                    ResultSet rs101=stmt1.executeQuery("show columns from course where field='Grade'");
                    if(rs101.next()){
                    stmt1.executeUpdate("ALTER TABLE course drop COLUMN Grade");
                    }
                    int i1=1,j1=3;
                    outputTextArea.setText("");
                    ResultSet rs100=stmt1.executeQuery("select * from course");
                    ResultSetMetaData rsMetaData = rs100.getMetaData();
                    int count = rsMetaData.getColumnCount();
                    outputTextArea.append("\tSNO\tstudentId\tstudentName\t");
                    while(j1<=count){
                        outputTextArea.append(rsMetaData.getColumnName(j1)+"\t");
                        j1+=1;
                    }
                    outputTextArea.append("\n");
                    
                    j1=3;
                    while(rs100.next()){
                        outputTextArea.append("\t"+i1+"\t"+rs100.getString("stdId")+"\t"+rs100.getString("stdName"));
                        while(j1<=count){
                            outputTextArea.append("\t"+rs100.getInt(rsMetaData.getColumnName(j1)));
                            j1+=1;
                        }
                        outputTextArea.append("\n");
                        i1+=1;
                        j1=3;
                    }
                }
                catch(SQLException e){System.out.println(e);}
            }
        });

        Home.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e1) {
                outputTextArea.setText("");
                outputTextArea.append("Home\n");
                outputTextArea.append("1)Adding student into course : Enter student name and student Id and click add student");
                outputTextArea.append("\n2)Adding subject into course : Enter subject total marks and click add subject. Enter the subject name in the Box and click ok");
                outputTextArea.append("\n3)Assigning marks to student : Enter student Id and subject name and marks awarded and click add marks");
                outputTextArea.append("\n4)Display the rankings: click the Display Rank button to calculate grades and display the rankings\n\n");
        
        }});


        addMarksButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e1) {
                try{
                String studentid = studentId.getText();
                String selectedSubject = (String) subjectComboBox.getSelectedItem();
                String marksSr = marksField.getText();
                ResultSet rs22=stmt1.executeQuery("select * from course where stdId='"+studentid+"'");
                if(rs22.next()){  
                    String studentName=rs22.getString("stdName"); 
                    ResultSet rs27=stmt1.executeQuery("select * from subjects where subName='"+selectedSubject+"'");
                    rs27.next();
                    int mMarks=rs27.getInt("subTotalMarks");
                    if (!studentid.isEmpty() && !marksSr.isEmpty()) {
                        try{int marksStr=Integer.parseInt(marksSr);
                        
                    if(marksStr>0 && marksStr<=mMarks){
                    
                        stmt1.executeUpdate("update course set "+selectedSubject+"="+marksStr+" where stdId='"+studentid+"'");
                        marksField.setText("");
                        //studentId.setText("");
                        outputTextArea.append("Added marks for " + studentName + " in " + selectedSubject + "\n");
                    }
                    else{outputTextArea.append("enter valid marks\n");}}
                    catch(NumberFormatException e5){outputTextArea.append("enter valid marks\n");}
                    }
                    else{outputTextArea.append("enter valid input\n");}//{enter valid input;}
                    }
                else{outputTextArea.append("Enter valid studentId\n");} //{ no stdid exists;}
            
                }
                catch(SQLException e){System.out.println(e);} 
            }
        });

        displayRankButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e1) {
                outputTextArea.setText(""); // Clear the output area
                getOverallGrade();
                if(stds.size()==0){outputTextArea.append("no students exist\n");}
                else{
                stds.get(0).displayResult(stds,stmt1,outputTextArea);}
        }});

        // Create and configure the GUI layout
        

        inputPanel.add(new JLabel("Student Name:"));
        inputPanel.add(studentNameField);
        inputPanel.add(new JLabel("Student Id:"));
        inputPanel.add(studentId);
        inputPanel.add(new JLabel("Subject:"));
        inputPanel.add(subjectComboBox);
        inputPanel.add(new JLabel("Subject total marks:"));
        inputPanel.add(subjectMaxMarks);
        inputPanel.add(new JLabel("Marks:"));
        inputPanel.add(marksField);

        buttonPanel.add(Home);
        buttonPanel.add(addStudentButton);
        buttonPanel.add(addSubjectButton);
        buttonPanel.add(addMarksButton);
        buttonPanel.add(displayRankButton);
        buttonPanel.add(displayStudents);

        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setTitle("Rank Display System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 500);
        setLocationRelativeTo(null); // Center the frame on the screen
        add(mainPanel);
        setVisible(true);
        Home.doClick();
    }

    public void getOverallGrade() {
        try{
            ResultSet rs101=stmt1.executeQuery("show columns from course where field='Grade'");
            if(!rs101.next()){
            stmt1.executeUpdate("ALTER TABLE course ADD COLUMN Grade double"); 
             }
            ResultSet rs44=stmt1.executeQuery("select * from subjects");
            ArrayList<String> subjects=new ArrayList<String>();
            ArrayList<Integer> maxMarks=new ArrayList<Integer>();
            HashMap<String, Double> grade = new HashMap<>();
            while(rs44.next()){
            subjects.add(rs44.getString("subName"));
            maxMarks.add(rs44.getInt("subTotalMarks"));
            }
            ResultSet rs45=stmt1.executeQuery("select * from course");
            double count=0;
            int i,flag=0;
            stds=new ArrayList<Student>();
            while(rs45.next())
            {
                i=0;
                count=0;
            while(i<maxMarks.size()){
                double t1=(double)rs45.getInt(subjects.get(i))/maxMarks.get(i);
                if(t1==0){
                    flag=1;
                    break;
                }
                else{
                    count+=t1;
                }
                i+=1;
            }
            count /= subjects.size();
            count *= 100;
            // stmt1.executeUpdate("update course set Grade="+count+" where stdId='"+rs45.getString("stdId")+"'");
            count=Math.round(count*100.00)/100.00;
            if(flag==1){count=-1;}
                grade.put(rs45.getString("stdId"), count);
                stds.add(new Student(count,rs45.getString("stdId")));
                flag=0;
            }
            for (Map.Entry<String, Double> e : grade.entrySet()){
                    stmt1.executeUpdate("update course set Grade="+e.getValue()+" where stdId='"+e.getKey()+"'");
            }
        }
        catch(SQLException e){System.out.println(e);}

    }


    
    
    public static void main(String[] args) throws ClassNotFoundException {
        try{
	    Class.forName("com.mysql.cj.jdbc.Driver");
	    Connection c=DriverManager.getConnection("jdbc:mysql://localhost:3306/varun","root","Varun@2004");
    	DatabaseMetaData dbm = c.getMetaData();
    	ResultSet rs = dbm.getTables(null, null, "course", null);
	    Statement stmt=c.createStatement();
	    if (rs.next()==false){
      		stmt.execute("CREATE TABLE course(stdName varchar(225),stdId varchar(10))");
		    stmt.execute("create table subjects(subName varchar(20),subTotalMarks int)");
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new RankDisplayingsSystem(stmt);
            }
            
        });
	    }
	    catch(SQLException e){System.out.println(e);}    
    }
}