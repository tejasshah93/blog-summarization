<?php
$q = $_REQUEST["url"];
$r = $_REQUEST["title"];
$hint = "";

if ($q != "") {
	//chmod("./tCrawler.py", 0777);
	//$hint = "inside";
	$fp = fopen("./demoFile", "w");
	$params = fopen("./params", "w");
	chmod("./demoFile", 0777);
	chmod("./params", 0777);
	$r =  iconv('ASCII', 'UTF-8//IGNORE', $r);
	fwrite($params, $r);
	fwrite($fp, "deleting and creating again\n");
	sleep(5);

	exec("rm -rf dataset Summary");
	exec("mkdir dataset");
	exec("touch Summary");
	exec("chmod -R 777 dataset Summary");
	$command = "./run.sh ".$q;
	exec($command);
	exec("rm -f Summary.aux Summary.log Summary.nav Summary.out Summary.pdf Summary.tex Summary.pyg Summary.snm Summary.toc");
	fwrite($fp, "executing python file\n");
	exec("python beamer.py");
	exec("chmod 777 Summary.tex");
	fwrite($fp, "pdflatex\n");
	exec("pdflatex Summary.tex");
	exec("chmod 777 Summary.pdf");
	exec("rm -f Summary.aux Summary.log Summary.nav Summary.out Summary.tex Summary.pyg Summary.snm Summary.toc");

	fclose($fp);
	fclose($params);
/*
	$command = "python ./tCrawler.py 2>&1";
	$pid = popen( $command,"r");
	while( !feof( $pid ) )
	{
		 echo fread($pid, 256);
	         flush();
		 ob_flush();
		 usleep(100000);
	}
	pclose($pid);*/

	$hint = file_get_contents("./Summary");
}
if ($hint == "") {
	$hint="Nopes :/";
}
echo $hint;
?>
