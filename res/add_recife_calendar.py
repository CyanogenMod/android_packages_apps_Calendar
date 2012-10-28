#!/usr/bin/env python
#coding:utf-8

import sys, os, shutil
import sys
import pycurl 
import subprocess,StringIO

from HTMLParser import HTMLParser 

def translate(frase, origem, destino):
    idiomaI = origem
    idiomaO = destino
    entrada	= frase
    palavra = entrada.replace(' ', '+')

    url = 'http://translate.google.com/translate_t?q=' + palavra + '&sl=' + idiomaI + '&tl=' + idiomaO

    class Parser(HTMLParser):
        cont = 0
        out = ''
        
        def handle_starttag(self, tag, attrs):
            if (tag == 'span' and len(attrs) > 0):
                for atom in attrs:
                    if (atom[0] == 'title' and atom[1] == entrada):
                        self.cont = 1
            else:
                self.cont = 0
        def handle_data(self, text):
            if (self.cont == 1):
                self.out = text
        def handle_endtag(self, tag):
            self.con = 0

    strio = StringIO.StringIO() 
    curlobj = pycurl.Curl()

    curlobj.setopt(pycurl.HTTPHEADER, [
          "User-Agent: Mozilla/5.001 (windows; U; NT4.0; en-us) Gecko/25250101",
          "Accept-Language: en-us,en;q=0.5",
        ])

    curlobj.setopt(pycurl.URL, url)
    curlobj.setopt(pycurl.WRITEFUNCTION, strio.write)
    curlobj.perform() 
    curlobj.close()

    p 	= Parser()
    aux	= p.feed( strio.getvalue() )

    return p.out

def traduz_recife(destino):
    frase = "Vou a Recife"

    origem = 'pt'
    palavra_completa = translate(frase, origem, destino)

    for palavra in palavra_completa.split(" "):
        palavra_atual = translate(palavra, destino, origem)
        if palavra_atual == "Recife": return palavra
    
    return None

def add_timezone(arrays_file, lingua):
    palavra = traduz_recife(lingua)

    if palavra == None:
        print "Língua não traduzida: " + lingua
        palavra = "Recife"

    arrays = open(arrays_file)
    
    found_tz = False
    found_tzv = False
    
    temp = open('temp.tmp', 'w')
    for line in arrays:
        temp.write(line)
        if 'name=\"timezone_labels\"' in line:
            counter_tz = 0
            found_tz = True
    
        if 'name=\"timezone_values\"' in line:
            counter_tzv = 0
            found_tzv = True
    
        if found_tz:
            counter_tz += 1
            if counter_tz == 22:
                temp.write("    <item>\"" + palavra + "\"</item>\n")
        
        if found_tzv:
            counter_tzv += 1
            if counter_tzv == 22:
                temp.write("    <item>\"America/Recife\"</item>\n")

                
    temp.flush()
    temp.close()

    arrays.flush()
    arrays.close()
    
    shutil.move('temp.tmp', arrays_file)
       
current_path = os.curdir

for e in os.listdir(current_path):
    if 'values' in e:

        if '-' in e:
            lingua = e.split('-')[1]
        else:
            lingua = 'en'

        values_path = current_path + '//' + e

        cem_por_cento = len(os.listdir(values_path))
        for filename in os.listdir(values_path):
            if filename == 'arrays.xml':
                add_timezone(values_path + '//' + filename, lingua)
