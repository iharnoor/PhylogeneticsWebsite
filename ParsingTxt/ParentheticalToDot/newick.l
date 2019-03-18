%{
#include "newick.tab.h"

static size_t string_length=0;
void* saferealloc(void *ptr, size_t size)
        {
        char* p= realloc(ptr,sizeof(char)*(size));
        if(p==NULL)
                {
                fprintf(stderr,"out of memory");
                exit(EXIT_FAILURE);
                }
        return p;
        }
static char* copy(const char* s,size_t length)
        {
        char* p= saferealloc(NULL,sizeof(char)*(length+1));
        strncpy(p,s,length);
        p[length]=0;
        return p;
        }

static char* append(size_t* len,const char* s2,size_t len2)
        {
        yylval.s= saferealloc( yylval.s,sizeof(char)*(*len+len2+1));
        strncpy(&yylval.s[*len],s2,len2);
        yylval.s[*len+len2]=0;
        *len+=len2;
        return yylval.s;
        }

%}

%s apos
%s quot

%%
<quot>{
\\\"    append(&string_length,"\"",1);
\'      append(&string_length,"\'",1);
\"      {BEGIN(INITIAL);return STRING;}
}

<apos>{
\\\'    append(&string_length,"\'",1);
\"      append(&string_length,"\"",1);
\'      {BEGIN(INITIAL);return STRING;}
}

<apos,quot>{
\\n     append(&string_length,"\n",1);
\\t     append(&string_length,"\t",1);
\\\\    append(&string_length,"\\",1);
([^\"\'\\]|\n)+ append(&string_length,yytext,yyleng);
        }

\:      return COLON;
\;      return SEMICOLON;
\)      return CPAR;
\(      return OPAR;
\,      return COMMA;
\"      {string_length=0;BEGIN(quot); }
\'      {string_length=0;BEGIN(apos); }
[a-zA-Z_][a-zA-Z_0-9]* {yylval.s=copy(yytext,yyleng); return STRING;}
[\+|\-]?[0-9]+  {yylval.d=copy(yytext,yyleng); return NUMBER;}
[\+|\-]?[0-9]+\.[0-9]+([e|E][0-9]+)? {yylval.d=copy(yytext,yyleng); return NUMBER;}
[ \t\n\r]       ;
.       {fprintf(stderr,"Syntax error (%c)\n",yytext[0]);exit(EXIT_FAILURE);}
%%