//package com.mkyong.basicwebcrawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.io.IOException;
import java.util.HashSet;

import java.util.*;

import java.util.ArrayList;
import java.io.FileWriter;
import java.util.List;



public class WebCrawler {

    private static HashSet<String> links; //All the links, used to detect duplicate links
    private static HashSet<Page> pages; //All the pages, used to detect duplicate pages
    private static int count; //The total number of pages we need
    private Page currentPage; //The current page we are on
    private static int totalUpVotes;
    
    public WebCrawler() {
        links = new HashSet<String>(); //Hash set of all the URLs
        pages = new HashSet<Page>();
        count = 0;
        totalUpVotes = 0;
    }

    public void getPageLinks(String URL) {
    	
        if (count < 200) {
        	if (!links.contains(URL)) { 
        	
        		try {
        			Document document = Jsoup.connect(URL).get();                            
        			if (links.add(URL)) {
                   
        				
        				//Get number of upvotes
        				String numUpVotes = "0";
        				Elements upVote = document.getElementsByClass("js-vote-count grid--cell fc-black-500 fs-title grid fd-column ai-center");
        				if (upVote.size() != 0)
        					numUpVotes = upVote.get(0).attr("data-value");

        				totalUpVotes += Integer.parseInt(numUpVotes);
        				
        				Page p = new Page(URL,numUpVotes);
        				
        				if (count != 0) {
        					p.addIncomingPage(currentPage);
        					p.addIncLinks();
        				}
        				pages.add(p);
        				currentPage = p;
        				
        				
        				count++;
        			}

                
        			//Get all links under the "Related tab"
        			Elements linked = document.getElementsByClass("related js-gps-related-questions");

            	
        			Elements l = linked.get(0).getElementsByClass("spacer");
            	
        			for (Element e : l) {
        				Elements l2 = e.getElementsByClass("question-hyperlink");
        				String toLink = "https://stackoverflow.com/" + l2.get(0).attr("href");
        				
        				currentPage.addNumOutGoing();
        				
        				getPageLinks(toLink);
            		
            		
        			}
                
            		
            	  
                
            } 
            catch (IOException e) {
                
            }
            
        }
       
        	else {
        		for (Page p : pages) {
        			if (URL.equals(p.url)) {
        				p.addIncLinks();
        				p.addIncomingPage(currentPage);
        			}
        		}
        	}
    }
    	
    }

    public static void main(String[] args) throws IOException{

    	FileWriter writer = new FileWriter("output.csv");
    	writer.append("URL, ");
    	writer.append("Page Rank, ");
    	writer.append("Previous Page Rank, ");
    	writer.append("Upvotes, ");
    	writer.append("Incoming Links, ");
    	writer.append("Outgoing Links\n");

    	new WebCrawler().getPageLinks("https://stackoverflow.com/questions/11227809/why-is-processing-a-sorted-array-faster-than-processing-an-unsorted-array");




    	for (int i = 0; i < 5; i++) {
    		//System.out.println("Iteration number: " + i + ")");
    		for (Page p : pages) {
    			
    			if (i == 0) {
    				p.setPr(1.0/(double)count);
    				p.setPrevPr(1.0/(double)count);



    			}
    			
    			else {
    				double pr = 0;
    				
    				for (int j = 0; j < p.incomingLinks; j++){
    					Page incomingPage = p.incomingPages.get(j);
    					double numerator = incomingPage.prevPr;
    					double denominator = incomingPage.outgoingLinks;
    					
    					pr += numerator/denominator;
    					//System.out.print(numerator + "/" + denominator + " + ");




    				}
    				//System.out.println();
    				p.setPrevPr(p.pr);
    				p.setPr(pr + 5*(p.upvotes/totalUpVotes));


				}



				writer.append(p.url + ", ");
				writer.append(p.pr + ", ");
				writer.append(p.prevPr + ", ");
				writer.append(p.upvotes + ", ");
				writer.append(p.incomingPages + ", ");
				writer.append(p.outgoingLinks + "\n");

    		}
    		
    		for (Page p : pages) {
    			p.setPrevPr(p.pr);


    		}

    	}
    	
    	ArrayList<Page> pageList = new ArrayList<>(pages);
    	Collections.sort(pageList);
    
    	for (int i = 0; i < pageList.size(); i++) {
    		Page p = pageList.get(i);
    		System.out.println(p.url);
    		System.out.println(p.upvotes);
    		System.out.println(p.pr);
    		System.out.println();

    	}
    	




		writer.flush();
    	writer.close();
    	
    	
    }
    
   
	
   
  }

