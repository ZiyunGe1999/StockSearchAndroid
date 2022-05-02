function setupHighCharts(jsonString, ticker, color) {
    data = JSON.parse(jsonString);
    price_chart_data = [];

    for (let i = 0; i < data.t.length; i++) {
        var tmp = [];
        tmp.push(data.t[i] * 1000);
        tmp.push(data.c[i]);
        price_chart_data.push(tmp);
      }

    Highcharts.chart('container', {
        title: {
          text: ticker + ' Hourly Price Variation'
        },
        yAxis: {
          title: false,
          opposite: true,
        },
        xAxis: {
            type: 'datetime',
            dateTimeLabelFormats: { // don't display the dummy year
                month: '%e. %b',
                year: '%b'
            },
        },
        tooltip: {
            split: true
          },
        legend: false,
        series: [{
          name: ticker,
          data: price_chart_data,
          type: 'line',
          color: color
        }]
      });
}

function setupText(jsonString) {
    // data = JSON.parse(jsonString);
    p = document.getElementById("test");
    p.innerHTML = jsonString;
}

// window.onload = function(){
//     setupHighCharts();
// }