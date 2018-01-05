package com.cloud.support.Template;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

public class CreateVirtualMachine {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		CreateVirtualMachine init=new CreateVirtualMachine();
		init.readInput();
		init.display();
		init.connect();
		
		String zoneUUID=init.getZoneID();
		System.out.println("ZoneID: "+zoneUUID);		
		Map<Integer, String> templates = init.getHyperVisorType(zoneUUID);				
		String templateUUID=init.getRequiredTemplate(templates);
		System.out.println(templateUUID); 
		Map<Integer, String> serviceOfferingList=init.getServiceOfferingID(zoneUUID);
		String serviceOffering=init.getRequiredServiceOffering(serviceOfferingList);
	}

	private String getRequiredServiceOffering(Map<Integer, String> serviceOfferingList) {
		// TODO Auto-generated method stub
		
		return null;
	}

	private Map<Integer, String> getServiceOfferingID(String zoneUUID) {
		// TODO Auto-generated method stub
		
		int i=0;
		String serviceOfferingSql="select name, display_text from service_offering where name not like '%System Offering%'";
		Map<Integer, String> serviceOfferings=new TreeMap<Integer, String>();
		try {
			stmt=(Statement) connection.createStatement();
			resultSet=stmt.executeQuery(serviceOfferingSql);
			while(resultSet.next()) {
				++i;
				serviceOfferings.put(new Integer(i),resultSet.getString("name"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return serviceOfferings;
	}

	private String getRequiredTemplate(Map<Integer, String> temps) {
		// TODO Auto-generated method stub
						
		int opt=0;
		Scanner option=new Scanner(System.in);
		String tempUUID=null;
				
		do {
			for(Integer i:temps.keySet()) {
				System.out.println(i+". "+temps.get(i));
			}
			System.out.println("Select template to deploy VM: ");
			opt=new Integer(option.next()).intValue();			
		} while(opt < 1 || opt > temps.size());
		
		String templateIDSql="select uuid from vm_template where name=\'"+temps.get(new Integer(opt))+"\'";
		try {
			stmt=(Statement) connection.createStatement();
			resultSet=stmt.executeQuery(templateIDSql);
			while(resultSet.next()) {
				tempUUID=resultSet.getString("uuid");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}						
		return tempUUID;
	}

	private Map<Integer, String> getHyperVisorType(String zoneID) {
		// TODO Auto-generated method stub
		String hypervisorListSql="select distinct hypervisor_type from cluster where data_center_id=(select id from data_center where uuid=\'"+zoneID+"\');";		
		Map<Integer,String> hypervisors=new TreeMap<Integer,String>();
		int i=0;
		try {
			stmt=(Statement) connection.createStatement();
			resultSet=stmt.executeQuery(hypervisorListSql);
			while(resultSet.next()) {
				hypervisors.put(new Integer(++i), resultSet.getString("hypervisor_type"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		hypervisors.put(hypervisors.size()+1, "None");
		Map<Integer, String> templateList = getTemplatesList(hypervisors);
		return templateList;
	}

	private Map<Integer, String> getTemplatesList(Map<Integer, String> hostVendor) {
		// TODO Auto-generated method stub
		
		String templateListSql=null;		
		Map<Integer,String> templatesList=new TreeMap<Integer, String>();
		
		for(Integer j:hostVendor.keySet()) {
			String hVendor = hostVendor.get(j);
			templateListSql="select name,format from vm_template where type in (\'USER\',\'BUILTIN\') and hypervisor_type=\'"+hVendor+"\' and state=\'Active\'";
			try {
				stmt=(Statement) connection.createStatement();
				resultSet=stmt.executeQuery(templateListSql);
				while(resultSet.next()) {
					if(resultSet.getString("format").equals("ISO")) {
						templatesList.put(new Integer(templatesList.size()+1), resultSet.getString("name")+"(ISO)");
					} else {					
						templatesList.put(new Integer(templatesList.size()+1), resultSet.getString("name"));
					}
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
		return templatesList;
	}	

	private String getZoneID() {
		// TODO Auto-generated method stub
		String zoneListSql="select name from data_center where allocation_state=\'Enabled\'";
		String targetZone=null;
		Map<Integer,String> zones=new TreeMap<Integer,String>();
		int i=0;
		try {
			stmt=(Statement) connection.createStatement();
			resultSet=stmt.executeQuery(zoneListSql);
			while(resultSet.next()) {
				zones.put(new Integer(++i),resultSet.getString("name"));		
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(zones.size()>1) {
			for(Integer j:zones.keySet()) {
				System.out.println(j.toString()+". "+zones.get(j));
			}
			Scanner zoneInput=new Scanner(System.in);
			System.out.println("Select Zone to deploy VM:");
			String zoneIndex=zoneInput.next();
			targetZone=zones.get(new Integer(zoneIndex));
		} else {
			targetZone=zones.get(new Integer("1"));
		}
		
		String zoneIDSql="select uuid from data_center where allocation_state=\'Enabled\' and name=\""+targetZone+"\";";
		String zoneID=null;
		try {
			stmt=(Statement) connection.createStatement();
			resultSet=stmt.executeQuery(zoneIDSql);
			while(resultSet.next()) {
				zoneID=resultSet.getString("uuid");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return zoneID;
	}

	private Statement stmt;
	private ResultSet resultSet;
	private String sql;
		
	private static Connection connection;

	private void connect() {
		// TODO Auto-generated method stub
		String url="jdbc:mysql://localhost:3306/";
		String database="cloud";
		try {			
			connection=(Connection) DriverManager.getConnection(url+database,user,pass);			
			System.out.println("Successfully connected to cloud database");
		} catch(Exception exc) {				
			System.out.println("Database connection failed");
			exc.printStackTrace();
		}
	}

	private String msIP;
	private String user;
	private String pass;

	private void display() {
		// TODO Auto-generated method stub
		System.out.println("Management Server IP: "+msIP);
		System.out.println("Username: "+user);
		System.out.println("Password: "+pass);
	}

	private void readInput() {
		// TODO Auto-generated method stub
		Scanner in=new Scanner(System.in);
		System.out.print("ManagementServer IP: ");
		msIP=in.next();
		System.out.print("Username: ");
		user=in.next();
		System.out.print("Password: ");
		pass=in.next();
	}
}
