#!/bin/bash
filesize=$(wc -l <$1)
linecount=1
hbracket="["
tcbracket="],"
tbracket="]"
while read -r line; do
  if (($linecount == 1)); then
    headers=$(echo $line | cut -d"," -f5-)
    OLDIFS=$IFS
    IFS=","
    ha=($headers)
    totalheaders=${#ha[@]}
    ((totalheaders--))
    ((totalheaders--))
    echo "var opscolumns = [" >opscolumns.data
    echo "var mbscolumns = [" >mbscolumns.data
    echo "{type: 'number', label: 'clock', color: 'black', disabledColor: 'lightgray', visible: true}," >>opscolumns.data
    echo "{type: 'number', label: 'clock', color: 'black', disabledColor: 'lightgray', visible: true}," >>mbscolumns.data
    hc=0
    cc=0
    colors=(red gold magenta indigo slateblue green olive teal blue navy brown crimson darkred peru maroon cornflowerblue steelblue darkgreen purple orange firebrick salmon deeppink coral tomato violet lime darkcyan darkblue chocolate darkmagenta blueviolet)
    for header in $headers; do
      if (($hc % 2)); then
        echo -n "{type: 'number', label: '$header', color: '${colors[${cc}]}', disabledColor: 'lightgray', visible: true}" >>mbscolumns.data
        if (($hc < $totalheaders)); then
          echo "," >>mbscolumns.data
        else
          echo "];" >>mbscolumns.data
        fi
        ((cc++))
      else
        echo -n "{type: 'number', label: '$header', color: '${colors[${cc}]}', disabledColor: 'lightgray', visible: true}" >>opscolumns.data
        if (($hc < $totalheaders)); then
          echo "," >>opscolumns.data
        else
          echo "];" >>opscolumns.data
        fi
      fi
      ((hc++))
    done
    echo "function drawChart () {" >>mbscolumns.data
    echo "if (!opschart) {" >ops.data
    echo "ops = new google.visualization.DataTable();" >>ops.data
    echo "ops.addColumn('number','clock');" >>ops.data
    echo "if (!mbschart) {" >mbs.data
    echo "mbs = new google.visualization.DataTable();" >>mbs.data
    echo "mbs.addColumn('number','clock');" >>mbs.data
    hc=0
    for header in $headers; do
      if (($hc % 2)); then
        echo "mbs.addColumn('number','$header');" >>mbs.data
        ((cc++))
      else
        echo "ops.addColumn('number','$header');" >>ops.data
      fi
      ((hc++))
    done
    echo "ops.addRows([" >>ops.data
    echo "mbs.addRows([" >>mbs.data
    IFS=$OLDIFS
  else
    OLDIFS=$IFS
    IFS=" "
    vc=1
    clock=$(echo $line | cut -d"," -f1)
    des=$(echo $line | cut -d"," -f5-)
    des="$des ,"
    opvalues="$clock"
    mbvalues="$clock"
    IFS=','
    for de in $des; do
      newvalue=$(echo $de | tr -d ' ')
      if [ $newvalue = "0" ]; then
        newvalue="null"
      fi
      if (($vc % 2)); then
        opvalues="$opvalues, $newvalue"
      else
        mbvalues="$mbvalues, $newvalue"
      fi
      ((vc++))
    done
    if (($linecount < $filesize)); then
      echo "$hbracket$opvalues$tcbracket" >>ops.data
      echo "$hbracket$mbvalues$tcbracket" >>mbs.data
    fi
    if (($linecount == $filesize)); then
      echo "$hbracket$opvalues$tbracket" >>ops.data
      echo "$hbracket$mbvalues$tbracket" >>mbs.data
    fi
    IFS=$OLDIFS
  fi
  ((linecount++))
done <$1
echo "], false);
      opsdataView = new google.visualization.DataView(ops);
                opschart = new google.visualization.LineChart(document.getElementById('opschart'));
                google.visualization.events.addListener(opschart, 'click', function (target) {
                    if (target.targetID.match(/^legendentry#\d+$/)) {
                        var index = parseInt(target.targetID.slice(12)) + 1;
                        opscolumns[index].visible = !opscolumns[index].visible;
                        mbscolumns[index].visible = !mbscolumns[index].visible;
                        drawChart();
                    }
                });
            }" >>ops.data
echo "], false);
               mbsdataView = new google.visualization.DataView(mbs);
                mbschart = new google.visualization.LineChart(document.getElementById('mbschart'));
                google.visualization.events.addListener(mbschart, 'click', function (target) {
                    if (target.targetID.match(/^legendentry#\d+$/)) {
                        var index = parseInt(target.targetID.slice(12)) + 1;
                        opscolumns[index].visible = !opscolumns[index].visible;
                        mbscolumns[index].visible = !mbscolumns[index].visible;
                        drawChart();
                    }
                });
            }" >>mbs.data

cat /Users/robert.faller/projects/IOLoader/content/chartheader.phtml ./opscolumns.data ./mbscolumns.data ./ops.data ./mbs.data /Users/robert.faller/projects/IOLoader/content/charttailer.phtml >./allops.html
