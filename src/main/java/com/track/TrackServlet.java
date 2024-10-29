package com.track;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.json.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class TrackServlet extends HttpServlet{
	
	//db connection
	static final String dbUrl = "jdbc:postgresql://localhost:5432/sanmauto";
	static final String dbUser = "postgres";
	static final String dbPass = "postql";
	
	//fedEx Api
	static final String fedAuthUrl = "https://apis-sandbox.fedex.com/oauth/token";
	static final String fedUser = "l7645e92d73dcd4d49b41c08844c3e15ac";
	static final String fedPass = "898316ea5bbf4f4a822563e30206b6cd";
	static final String fedTrackUrl = "https://apis-sandbox.fedex.com/track/v1/trackingnumbers";
	
	//After Api
	

	
	//Checkpoints
	int existDelivered = 0;
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//Handling Multiple Request
		//PrintWriters used to return the results to the front end
		PrintWriter printOut = response.getWriter();
		
		//getting input from front end
		String carrier_inp = request.getParameter("carrier_details");
		String trackNum_inp = request.getParameter("track_numbers");
		String[] arrayTrackNumbers = trackNum_inp.split(",");
		
//		printOut.println(carrier_inp);
//		printOut.println(trackNum_inp);
		
		if(carrier_inp.contains("FedEx")) {
			
			//Db initializers
			Connection fedCon = null;
			PreparedStatement prepStmt = null;
			ResultSet res = null;
			String selQry = "SELECT * FROM tracksphere WHERE waybillnumber = ?";
			String updQry = "UPDATE * FROM tracksphere";
			
			//db values
			String fedPlatform = null;
			String fedCarrier = null;
			String fedWaybill = null;
			String fedCurrentStat = null;
			String fedMiltestone = null;
			String fedExpectDate = null;
			String fedDelSum = null;
			
			//Result JSONObject to front-end
			JSONArray resArr = new JSONArray();
			JSONObject resJson = new JSONObject();
			
			
			
			for(String fedWay : arrayTrackNumbers) {
				
				try {

					fedCon = DriverManager.getConnection(dbUrl, dbUser, dbPass);
					prepStmt = fedCon.prepareStatement(selQry);
					prepStmt.setString(1, fedWay);
					res = prepStmt.executeQuery();
					
					while(res.next()) {
						
						if(res.getString("currentstatus").equalsIgnoreCase("Delivered")) {
							
							fedPlatform = res.getString("platform");
							fedCarrier = res.getString("carrier");
							fedWaybill = res.getString("waybillnumber");
							fedCurrentStat = res.getString("currentstatus");
							fedMiltestone = res.getString("milestone");
							fedExpectDate = res.getString("expecteddeldate");
							fedDelSum = res.getString("deliverysummary");
							
							resJson.put("platForm", fedPlatform);
							resJson.put("carrierCode", fedCarrier);
							resJson.put("waybillNumber", fedWaybill);
							resJson.put("currentStatus", fedCurrentStat);
							resJson.put("milestone", fedMiltestone);
							resJson.put("estimatedDeliveryDate", fedExpectDate);
							resJson.put("deliverySummary", fedDelSum);
							
							existDelivered = 1;
							
							resArr.add(resJson);
							
						}else {
							
							
							
						}
						
					}
					
					fedCon.close();
					prepStmt.close();
					res.close();
					
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
				
			}
			
			
			//returning value to ajax success function in JsonObject
			if(existDelivered == 1) {
				printOut.println(resArr);
			}
					
				
			
		}else if(carrier_inp.contains("Aftership")){
			
			for(String aft_trNum : arrayTrackNumbers) {
				
//				printOut.println("AfterShip Track Numbers : " + aft_trNum);
				
				//Check whether the waybill number is already tracked and delivered from db data
				
				
			}
			
		}
		
	}

}
