/* Change this if the SERVER_NAME environment variable does not report
	the true name of your web server. */
#if 1
#define SERVER_NAME "dna.engr.uconn.edu"
#endif

/* You may need to change this, particularly under Windows;
	it is a reasonable guess as to an acceptable place to
	store a saved environment in order to test that feature. 
	If that feature is not important to you, you needn't
	concern yourself with this. */

#define SAVED_ENVIRONMENT "/tmp/cgicsave.env"

#include <stdio.h>
#include "cgic.h"
#include <stdlib.h>

#define MAX_LINE_LENGTH 1000

int done = 0;
int nRuns1_val = 20;
int nRuns2_val = 20;
double sig_val = 0.05;


void HandleSubmit();
void ShowForm();


int cgiMain() {

        if( cgiRemoteAddr == NULL )
        {
                fprintf(cgiOut, "Error: REMOTE_ADDR missing\n");
                return -1;
        }

	/* Send the content type, letting the browser know this is HTML */
	cgiHeaderContentType("text/html");
	/* Top of the page */
	fprintf(cgiOut, "<HTML><HEAD>\n");
	fprintf(cgiOut, "<TITLE>Bootstrap support calculator</TITLE></HEAD>\n");

	/* If a submit button has already been clicked, act on the 
		submission of the form. */
	if (cgiFormSubmitClicked("continue") == cgiFormSuccess) 
	{
		HandleSubmit();
	}


	/* show the form */
	ShowForm();
 
	/* Finish up the page */
	fprintf(cgiOut, "</BODY></HTML>\n");

	return 0;
}

void HandleSubmit()
{
        char filename[1000];
        char command[1000];

        cgiFormInteger("nRuns1", &nRuns1_val, nRuns1_val);
        if( nRuns1_val <= 0 ) {
                nRuns1_val = 1;
        }                  
        cgiFormInteger("nRuns2", &nRuns2_val, nRuns2_val);
        if( nRuns2_val <= 0 ) {
                nRuns2_val = 1;
        }                  
        cgiFormDouble("sig", &sig_val, sig_val);
        if( sig_val < 0.000001 ) {
                sig_val = 0.000001;
        }                  
        if( sig_val > 1 ) {
                sig_val = 1;
        }                  

        sprintf(filename, "/tmp/calc.support.%s.out", cgiRemoteAddr);

        sprintf(command, "java -cp /home/software/public_html/cgi-bin/calc/ support %d %d %f %s", nRuns1_val, nRuns2_val, sig_val, filename );
        if( system(command) == -1)
        {
          fprintf(cgiOut, "Error running support calculator on %s\n", SERVER_NAME );
          exit(0);
        }

	done = 1;
} 


void ShowForm()
{
        char command[1000];
        char filename[1000];
        char line[MAX_LINE_LENGTH+1];
        FILE *f1;

if( done == 0 ) {

        fprintf(cgiOut, "<!-- 2.0: multipart/form-data is required for file uploads. -->");
        fprintf(cgiOut, "<form method=\"POST\" enctype=\"multipart/form-data\" ");
        fprintf(cgiOut, "       action=\"");
        cgiValueEscape(cgiScriptName);                
        fprintf(cgiOut, "?%s\">\n", cgiQueryString);

        fprintf(cgiOut, "<p>\n");
        fprintf(cgiOut, "Bootstrap runs for condition 1: \n" );
        fprintf(cgiOut, "<input type=\"text\" name=\"nRuns1\" value=\"20\">\n");
	   fprintf(cgiOut, "<br>\n");
        fprintf(cgiOut, "Bootstrap runs for condition 2: \n" );
        fprintf(cgiOut, "<input type=\"text\" name=\"nRuns2\" value=\"20\">\n");
	   fprintf(cgiOut, "<br>\n");
        fprintf(cgiOut, "Significance level: \n" );
        fprintf(cgiOut, "<input type=\"text\" name=\"sig\" value=\"0.05\">\n");
	   fprintf(cgiOut, "<br>\n");
	   fprintf(cgiOut, "<p>\n");

        fprintf(cgiOut, "<input type=\"submit\" name=\"continue\" value=\"Compute support\">\n");
        fprintf(cgiOut, "<input type=\"reset\" value=\"Reset\">\n");
	fprintf(cgiOut, "</form>\n");
 
}
if( done == 1 ) {

        sprintf(filename, "/tmp/calc.support.%s.out", cgiRemoteAddr);

        f1=fopen(filename,"r");
        if( f1 == NULL )
        {
          fprintf(cgiOut, "Error opening temporary output file\n" );
          exit(0);
        }

        fprintf(cgiOut, "<pre>\n");
        while( fgets( line, MAX_LINE_LENGTH, f1) )
        {
            fprintf(cgiOut, "%s", line);
        }
        fprintf(cgiOut, "</pre>\n");

        fclose(f1);

        sprintf(command, "rm %s",
                filename);
        if( system(command) == -1)
        {
          exit(0);
        }


        fprintf(cgiOut, "<div align=\"center\"><center>");
        fprintf(cgiOut, "<table border=\"2\" cellpadding=\"0\" cellspacing=\"0\" ");
        fprintf(cgiOut, "<tr><td bgcolor=\"B0B0B0\" width=\"100%%\" height=\"1\">");
        fprintf(cgiOut, "<font  face=\"Arial\"><a href=\"http://dna.engr.uconn.edu/~software/cgi-bin/calc/calc.cgi\">");
        fprintf(cgiOut, "Run Again</a></font></td>");
        fprintf(cgiOut, "</tr></table></center></div>\n");
}

}
