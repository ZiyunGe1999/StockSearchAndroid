function setupHighCharts(jsonString) {
    data = JSON.parse(jsonString);

    trendTimes = [];
    strong_buy = [];
    buy  = [];
    hold  = [];
    sell  = [];
    strong_sell = [];
    for (let i = 0; i < data.length; i++) {
        var item = data[i];
        trendTimes.push(item.period);
        strong_buy.push(item.strongBuy);
        buy.push(item.buy);
        hold.push(item.hold);
        sell.push(item.sell);
        strong_sell.push(item.strongSell);
    }

    Highcharts.chart('container', {
        chart: {
          type: 'column'
        },
        title: {
            text: 'Recommendation Trends'
        },
        xAxis: {
            type: 'datetime',
            categories: trendTimes
            // dateTimeLabelFormats: {
            //   month: '%e. %b',
            //   year: '%b'
            // }
        },
        yAxis: {
            min: 0,
            title: {
                text: '#Analysis'
            },
            stackLabels: {
                enabled: true,
                style: {
                    fontWeight: 'bold',
                    color: ( // theme
                        'red' && 'blue'
                    ) || 'gray'
                }
            }
        },
        legend: {
            align: 'center',
            x: 0,
            verticalAlign: 'bottom',
            y: 0,
            floating: false,
            backgroundColor:
                'white',
            borderColor: '#CCC',
            borderWidth: 1,
            shadow: false
        },
        tooltip: {
            headerFormat: '<b>{point.x}</b><br/>',
            pointFormat: '{series.name}: {point.y}<br/>Total: {point.stackTotal}'
        },
        plotOptions: {
            column: {
                stacking: 'normal',
                dataLabels: {
                    enabled: true
                }
            }
        },
        series: [{
            name: 'Strong Buy',
            data: strong_buy,
            type: 'column',
            color: '#1C6D37',
        }, {
            name: 'Buy',
            data: buy,
            type: 'column',
            color: '#20AD50'
        }, {
            name: 'Hold',
            data: hold,
            type: 'column',
            color: '#A17A15'
        }, {
            name: 'Sell',
            data: sell,
            type: 'column',
            color: '#C74849'
        }, {
            name: 'Strong Sell',
            data: strong_sell,
            type: 'column',
            color: '#67272A'
        }]
      });
}