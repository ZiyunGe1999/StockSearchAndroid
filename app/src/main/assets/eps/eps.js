function setupHighCharts(jsonString) {
    data = JSON.parse(jsonString);

    actual = [];
    estimate = [];
    for (let item of data) {
        if (item.actual) {
        actual.push(item.actual);
        }
        else {
        actual.push(0);
        }
        if (item.estimate) {
        estimate.push(item.estimate);
        }
        else {
        estimate.push(0);
        }
    }

    Highcharts.chart('container', {
        chart: {
          type: 'line',
          // inverted: true
        },
        title: {
            text: 'Historical EPS Surprises'
        },
        // subtitle: {
        //     text: 'According to the Standard Atmosphere Model'
        // },
        xAxis: {
            // reversed: false,
            // title: {
            //     enabled: true,
            //     text: 'Altitude'
            // },
            // labels: {
            //     format: '{value} km'
            // },
            // accessibility: {
            //     rangeDescription: 'Range: 0 to 80 km.'
            // },
            // maxPadding: 0.05,
            // showLastLabel: true
            type: 'datetime',
            categories: ['2022-12-31', '2022-09-30', '2022-06-30', '2022-03-31']
        },
        yAxis: {
            title: {
                text: 'Quarterly EPS'
            },
            min: 0,
            // labels: {
            //     format: '{value}°'
            // },
            // accessibility: {
            //     rangeDescription: 'Range: -90°C to 20°C.'
            // },
            // lineWidth: 2
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
            // headerFormat: `<b>{xAxis.categories}</b><br/>\n
            //               Surprise: {series[0].data}`,
            // pointFormat: `{xAxis.categories[point.x - 1]}<br/>
            //              Surprise: {series[0].data}`,
            xDateFormat: '%Y-%m-%d',
            shared: true
    
        },
        // plotOptions: {
        //     spline: {
        //         marker: {
        //             enable: true
        //         }
        //     }
        // },
        series: [
          // {
          //   name: 'Surprise',
          //   data: this.surprise,
          //   type: 'line',
          //   visible: false
          // },
        {
          name: 'Actual',
          data: actual,
          type: 'line'
        },{
          name: 'Estimate',
          data: estimate,
          type: 'line'
        }],
      });
}