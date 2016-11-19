/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.me.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import org.elasticsearch.node.Node;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;
import org.elasticsearch.search.SearchHit;

/**
 *
 * @author Sandy1
 */
public class DisplayResults extends HttpServlet{
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    public int a =10;
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String query=request.getParameter("query");
        String queryID = request.getParameter("queryID");
        String assessor = request.getParameter("assessorID");
        try {
            out.println("<html><head>");
            out.println("<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap.min.css\">");
            out.println("<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap.min.css\">");
            out.println("</head>");
            out.println("<body>");
            out.println("<div class=\"panel panel-default\"><div class=\"panel-body\">");
            out.println("<center><h2>Assessed By: " + assessor + "</h2></center>");
            out.println("<center><h2>Results for the query ID: " + queryID + "</h2></center>");
            out.println("<center><h2>Query: " + query + "</h2></center>");
            out.println("</div></div>");
            out.println("<div class=\"panel panel-default\"><div class=\"panel-body\">");
            out.println("<center><h3>Results</h3></center>");
            out.println("<form action='WriteToFile' method='post'>");
            out.println("<input type='hidden' name='assessor' value='" + assessor + "'>");
            out.println("<input type='hidden' name='queryID' value='" + queryID + "'>");
            out.println("<center><button class='btn btn-default'>Write To File</button></center>");
            out.println("<table class='table-striped form-group' width='100%'>");
            out.println("<tr><th>SNO</th><th>URL</th><th>Link to Page</th><th>Grade</th></tr>");
            getESValues(query, out);
            out.println("</table>");
            out.println("</form>");
            out.println("</div></div>");
            out.println("</body></html>");
        } finally {
            out.close();
        }
        
    }
    
    public void getESValues(String query, PrintWriter out) {
        Node node = nodeBuilder().client(true).clusterName("harvardcluster").node();
		Client client = node.client();
		QueryBuilder qb = matchQuery(
			    "text", 
			    query
			);
                
		
		SearchResponse scrollResp = client.prepareSearch("harvard").setTypes("document")
			    .setScroll(new TimeValue(6000))
			    .setQuery(qb)
                            .addField("")
			    
			    .setSize(200).execute().actionGet(); //100 hits per shard will be returned for each scroll
		
		if (scrollResp.getHits().getTotalHits() == 0) {
                out.println("<h3>No relevant documents found!</h3>");
        }
               
                int count=1;
		
        while (true) {
            for (SearchHit hit : scrollResp.getHits().getHits()) {
            	//String txt = (String) hit.getSource().get("text");
            	
                String docno = (String) hit.getId();
                out.println("<tr>");
                out.println("<td>" + count + "</td>");
                out.println("<td><input type='text' name='urls[]' value='" + docno + "' class='form-control' style='\"outline:none\", border:\"none\";'></input></td>");
                out.println("<td><a href='"+ docno + "' target='_blank'>Link to page</a>"+"</td>");
                out.println("<td>");
                out.println("<select name='grade[]'value='0'>");
                out.println("<option value='0'>0</option>");
                out.println("<option value='1'>1</option>");
                out.println("<option value='2'>2</option>");
                out.println("</select>");
                out.println("</td></tr>");
                count=count+1;
                if (count>=201) {
                    break;
                }
                
            }
            scrollResp =
		client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(
									       new TimeValue(6000)).execute().actionGet();
            if (scrollResp.getHits().getHits().length == 0) {
                break;
            }
        }
			node.close();
        
    }
        
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
