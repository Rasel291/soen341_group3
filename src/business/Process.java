package business;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import model.JavaSQLite;
import model.Quotations;
import model.Requests;

public class Process extends JFrame {
	private static final long serialVersionUID = 1L;
	JButton button = new JButton();
	DefaultTableModel model2;
	int seqItemRequest = 0;
	Requests requests = new Requests();
	Quotations quotations = new Quotations();
	DefaultTableModel modelRq = new DefaultTableModel();
	JTable tableRequests = new JTable();
	/**
	 * Build all requests table
	 * @return
	 */
	public JScrollPane buildGridRequest() {
		String[] columnNames = {"Request Id", "Total", "Status", "Description"};
		modelRq.setColumnIdentifiers(columnNames);
        tableRequests.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tableRequests.setFillsViewportHeight(true);
        
        JScrollPane scroll = new JScrollPane(tableRequests);
        scroll.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        requests.getAllRequests(modelRq);
        tableRequests.setModel(modelRq);
        
        return scroll;
	}
	
	public Process() {
		// TODO Auto-generated constructor stub
		String[] columnNames = {"No.", "Name", "Unit", "Qty"};
		JFrame frame1 = new JFrame("Procurement System");
        frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
//        frame1.setLayout(new SpringLayout());
        DefaultTableModel model = new DefaultTableModel();
        model.setColumnIdentifiers(columnNames);
        JTable table = new JTable();
        table.setModel(model);
//        table.getColumn("Action").setCellRenderer(new ButtonRenderer());
//        table.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox()));
        		        
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setFillsViewportHeight(true);
        
        JScrollPane scroll = new JScrollPane(table);
        scroll.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        JPanel outPanel = new JPanel();
        outPanel.setLayout(new BoxLayout(outPanel, BoxLayout.PAGE_AXIS));
        JPanel makeRequestPanel = new JPanel();
        makeRequestPanel.setLayout(new BoxLayout(makeRequestPanel, BoxLayout.PAGE_AXIS));

        String itemName = "";
        int itemQty = 0;
        Statement stmt = null;
        ResultSet rs = null;
        Connection con = JavaSQLite.connectDB();
        try {
//        	PreparedStatement pst = con.prepareStatement("select * from items");
//            ResultSet rs = pst.executeQuery();
        	stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT distinct name FROM quotations");
            int seq = 0;
            while (rs.next()) {
            	seq ++;
                itemName = rs.getString("name");
                model.addRow(new Object[]{seq, itemName, "Kg", itemQty});
            }
            if (seq < 1) {
                JOptionPane.showMessageDialog(null, "No Record Found", "Error", JOptionPane.ERROR_MESSAGE);
            }
            if (seq == 1) {
                System.out.println(seq + " Record Found");
            } else {
                System.out.println(seq + " Records Found");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
        	try {
				stmt.close();
				rs.close();
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        // Submit button
        JButton btnCalculate = new JButton("Submit");
        
        // table 2
        String[] columnNames2 = {"No.", "Name", "Unit", "Price", "Qty", "Price * Qty"};
        
        JTable table2 = new JTable();
        
        table2.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table2.setFillsViewportHeight(true);
        
        // result table
        JScrollPane scroll2 = new JScrollPane(table2);
        scroll2.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll2.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
//        model2.addRow(new Object[]{seq, itemName, itemPrice, itemQty});
        
        JPanel topPanel = new JPanel(new SpringLayout());
        JLabel label = new JLabel("The lowest quotation:");
        topPanel.add(label, BorderLayout.BEFORE_LINE_BEGINS);
        
        JLabel lblFirst = new JLabel("Enter quantity of items you need:");
        
        makeRequestPanel.add(lblFirst);
        makeRequestPanel.add(scroll);
        makeRequestPanel.add(btnCalculate);
        JButton btnViewRequests = new JButton("View all requests");
        makeRequestPanel.add(btnViewRequests);
        makeRequestPanel.add(label);
        
        btnCalculate.addActionListener(
	      new ActionListener()
	      {
	        public void actionPerformed(ActionEvent event)
	        {
	        	model2 = new DefaultTableModel();
	        	model2.setColumnIdentifiers(columnNames2);
	        	table2.setModel(model2);
	        	Object[] columnData = new Object[table.getRowCount()];  // One entry for each row
//	            Object[] rowData = new Object [table.getRowCount()];
	            for (int i = 0; i < table.getRowCount(); i++) {  // Loop through the rows
	                // 
	            	if(Integer.parseInt(table.getValueAt(i, 3).toString()) > 0) {
	            		columnData[i] = table.getValueAt(i, 1) + "-" + table.getValueAt(i, 3);
	            	}
	             }
	            Quotations[] lowestQuotationObj = quotations.getTheLowestQuotation(columnData);
	            int seqNo = 0;
	            String supplierName = "";
	            int total = 0;
	            for(Quotations q: lowestQuotationObj) {
	            	if(q != null) {
        				seqNo ++;
        				total += Integer.parseInt(q.getPrice()) * Integer.parseInt(q.getQty());
        				model2.addRow(new Object[] {seqNo, q.getName(), q.getUnit(), q.getPrice(), q.getQty(), 
        						Integer.parseInt(q.getPrice()) * Integer.parseInt(q.getQty())});
        				if("".equals(supplierName)) {
        					supplierName = q.getSupplier();
        				}
	            	}
	            }
	            model2.addRow(new Object[] {"", "", "", "", supplierName + " - Total:", total});
	        	if(total > 0 && total < 5000) {
	        		JOptionPane.showMessageDialog(null, "The lowest quotation from "+ supplierName + ": $" + total + " \nYour request is approved!");
	        		// save to DB - table Requests
	        		requests.saveRequest(total, 1, "This request is approved!");
	        	} else if(total > 5000) {
	        		JOptionPane.showMessageDialog(null, "The lowest quotation from "+ supplierName + ": $" + total + " \nYour request is pending as it is greater than $5000");
	        		requests.saveRequest(total, 2, "This request is pending for approval");
	        	} else {
	        		JOptionPane.showMessageDialog(null, "Please enter the quantity for items");
	        	}
	        }
	      }
	    );       
        
        makeRequestPanel.add(scroll2);
        makeRequestPanel.add(Box.createRigidArea(new Dimension(0,5)));
        outPanel.add(makeRequestPanel);
        
        // View all requests panel
        JPanel viewRequestsPanel = new JPanel();
        viewRequestsPanel.setLayout(new BoxLayout(viewRequestsPanel, BoxLayout.PAGE_AXIS));
        JLabel lblViewRQ = new JLabel("All requests:");
        viewRequestsPanel.add(lblViewRQ);
        viewRequestsPanel.add(buildGridRequest());
        JButton btnBackMakeRequest = new JButton("Back to make new request");
        viewRequestsPanel.add(btnBackMakeRequest);
        viewRequestsPanel.add(Box.createRigidArea(new Dimension(10,0)));
        viewRequestsPanel.setVisible(false);
        
        outPanel.add(viewRequestsPanel);
        
        frame1.add(outPanel);
        frame1.setVisible(true);
        frame1.setSize(400, 300);
        
        btnViewRequests.addActionListener(
	      new ActionListener()
	      {
	        public void actionPerformed(ActionEvent event)
	        {
	        	makeRequestPanel.setVisible(false);
	        	viewRequestsPanel.setVisible(true);
	        	((DefaultTableModel)tableRequests.getModel()).setRowCount(0);
	        	tableRequests.setModel(requests.getAllRequests(modelRq));
	        }
	      }
	    );
        btnBackMakeRequest.addActionListener(
  	      new ActionListener()
  	      {
  	        public void actionPerformed(ActionEvent event)
  	        {
  	        	makeRequestPanel.setVisible(true);
  	        	viewRequestsPanel.setVisible(false);
  	        }
  	      }
  	    ); 
	}
	
	class ButtonRenderer extends JButton implements TableCellRenderer {
	    public ButtonRenderer() {
	      setOpaque(true);
	    }
	  public Component getTableCellRendererComponent(JTable table, Object value,
	    boolean isSelected, boolean hasFocus, int row, int column) {
	      setText((value == null) ? "Select" : value.toString());
	      return this;
	    }
  }
  class ButtonEditor extends DefaultCellEditor 
  {
    private String label;
    
    public ButtonEditor(JCheckBox checkBox)
    {
      super(checkBox);
    }
    public Component getTableCellEditorComponent(JTable table, Object value,
    boolean isSelected, int row, int column) 
    {
    	seqItemRequest ++;
    	model2.addRow(new Object[]{seqItemRequest, table.getValueAt(row, 1), table.getValueAt(row, 2)});
    	label = (value == null) ? "Select" : value.toString();
    	button.setText(label);
    	button.setEnabled(false);
    	button.addActionListener(
  	      new ActionListener()
  	      {
  	        public void actionPerformed(ActionEvent event)
  	        {
  	        	System.out.println("button clicked!");
  	        	JOptionPane.showMessageDialog(null,"");
  	        }
  	      }
  	    );
    	return button;
    }
    public Object getCellEditorValue() 
    {
    	System.out.println("getCellEditorValue");
    	button.setEnabled(false);
    	return new String(label);
    }
  }
	
	public static void main(String[] args) {
		new Process();
	}

}
