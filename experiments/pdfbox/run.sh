#!/bin/bash
# define workload PDFs
workloads=(./workload/000752 ./workload/000753 ./workload/000809 ./workload/000810 ./workload/000817)
overlay_pdf="./workload/000142"
extension=".pdf"
pdfbox_jar_loc="./pdfbox-app-3.0.0-beta1.jar"

fingerprints="classfile.sha256.jsonl"
javaagent="-javaagent:watchdog-agent-0.8.1-SNAPSHOT.jar=fingerprints=${fingerprints},skipShutdown=true"

# execute PDFBox functionalities for each workload
for i in ${workloads[@]}
do
  # Encrypt
  echo "Encrypt " ${i}${extension}
  java ${javaagent} -jar ${pdfbox_jar_loc} encrypt -O 123 -U 123 --input "${i}"${extension} --output ${i}-locked${extension}

  # Decrypt
  echo "Decrypt " ${i}${extension}
  java ${javaagent} -jar ${pdfbox_jar_loc} decrypt -password 123 --input "${i}"-locked${extension} --output ${i}-unlocked${extension}

  # ExtractText
  echo "ExtractText " ${i}${extension}
  java ${javaagent} -jar ${pdfbox_jar_loc} export:text -password 123 -sort --input "${i}"-locked${extension} --output ${i}-from-pdf.txt

  # ExtractImages
  echo "ExtractImages " ${i}${extension}
  java ${javaagent} -jar ${pdfbox_jar_loc} export:images -password 123 -useDirectJPEG --input "${i}"-locked${extension}

  # PDFToImage
  echo "PDFToImage " ${i}${extension}
  java ${javaagent} -jar ${pdfbox_jar_loc} render -password 123 -format png -prefix workloadAsImage -time --input "${i}"-locked${extension}

  # TextToPDF
  echo "TextToPDF " ${i}${extension}
  java ${javaagent} -jar ${pdfbox_jar_loc} fromtext -standardFont Helvetica-BoldOblique -fontSize 18 --output "${i}"-from-txt${extension} --input ${i}-from-pdf.txt

  # PDFSplit
  echo "PDFSplit " ${i}${extension}
  java ${javaagent} -jar ${pdfbox_jar_loc}  split -password 123 -split 1 --input "${i}"-locked${extension}

  # PDFMerger
  # Bug with wildcard
  # echo "PDFMerger " ${i}${extension}
  # java ${javaagent} -jar ${pdfbox_jar_loc} merge --input "${i}-locked-*${extension}" --output ${i}-merged${extension}

  # # WriteDecodedDoc
  # echo "WriteDecodedDoc " ${i}${extension}
  # java ${javaagent} -jar ${pdfbox_jar_loc} WriteDecodedDoc -password 123 ${i}-locked${extension} ${i}-decoded${extension}

  # OverlayPDF
  # Keeps throwing an error "No input document"
  # echo "OverlayPDF " ${i}${extension}
  # java ${javaagent} -jar ${pdfbox_jar_loc} overlay --input ${i}${extension} -default ${overlay_pdf}${extension} -position FOREGROUND --output ${i}-overlaid${extension}
done
