for i in {1..12}; do 
	convert ss$i.png ss$i.ps
	ps2eps -f --fixps ss$i.ps
done 
