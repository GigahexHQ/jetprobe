(function($) {
  $(function() {

    $('.button-collapse').sideNav();
    //select the active suite
    var activeSuite = $('.nav-options > ul > li.active').attr("data-suite")

    //Load the stats for the active suite
    $("div.statusbar-top").children("div.col").each(function(i, elem) {
      var childSuite = $(elem).children("div").attr("data-suite-parent")
      if (childSuite == activeSuite) {
          $(elem).removeClass("hidden")
      }
    })

    //Load the validation results panel
    loadResultsPanel(activeSuite);
    //$("#result-panel").children("li[data-suite-panel='" + activeSuite + "']").removeClass("hidden")


    $('.nav-options > ul > li').on('click', function() {
      var suiteName = $(this).attr("data-suite");
      //Add the active class to the navigation
      $(this).addClass("active")
      $(this).siblings("li").removeClass("active")

      //Load the stats for the suite
      $("div.statusbar-top").children("div.col").each(function(i, elem) {
        var childSuite = $(elem).children("div").attr("data-suite-parent")
        if (childSuite != suiteName) {
          if (!$(elem).hasClass("hidden")) {
            $(elem).addClass("hidden")
          }
        } else {
          $(elem).removeClass("hidden")
        }
      });
      console.log("clicked on " + suiteName)

      //load the validation result panel
      loadResultsPanel(suiteName)
      $(".result-panel").each(function(i,panel){
        if($(panel).attr("data-results-panel") == suiteName){
          console.log("removing the panel ")
          $(panel).removeClass("hidden");
        } else{
          if(!$(panel).hasClass("hidden")){
            $(panel).addClass("hidden");
          }
        }
      });
      //$("#result-panel").children("li[data-suite-panel='" + suiteName + "']").removeClass("hidden")
    });
    var totalSum = 0;
    //Colors for stats
    //[passed,failed,skipped]
    var colors = {
      passed: '#4dae51',
      failed: '#d7451c',
      skipped: '#ffaa42'
    }
    //find all the stats elements
    $('.stats-container').each(function(index, elem) {
      var stat = $(elem).attr('data-stat')
      totalSum = totalSum + parseInt(stat)
    });

    //Display the element
    $('.stats-container').each(function(index, elem) {
      var stat = $(elem).attr('data-stat')
      var status = $(elem).attr('data-status')
      var fraction = parseFloat($(elem).attr('data-fraction'))
      var suiteName = $(elem).attr('data-suite-parent')
      var selector = "stats-" + suiteName + '-' + status

      var finalStatus = status.substr(0, 1).toUpperCase() + status.substr(1, status.length)
      var element = `<div id="${selector}" class="stats-bar">
        </div>
        <div class="stats">
          <h3> ${stat}</h3>
          <p>${finalStatus}</p>
        </div>`
      $(this).append(element);
      createBar('#' + selector, colors[status], fraction);
    });


  }); // end of document ready
})(jQuery); // end of jQuery name space

function loadResultsPanel(suiteName){
  $(".result-panel").each(function(i,panel){
    if($(panel).attr("data-results-panel") == suiteName){
      console.log("removing the panel ")
      $(panel).removeClass("hidden");
    } else{
      if(!$(panel).hasClass("hidden")){
        $(panel).addClass("hidden");
      }
    }
  });
}

function createBar(selector, paintColor, fraction) {
  var bar = new ProgressBar.Circle(selector, {
    color: '#aaa',
    // This has to be the same size as the maximum width to
    // prevent clipping
    strokeWidth: 4,
    trailWidth: 4,
    easing: 'easeInOut',
    duration: 1400,
    text: {
      autoStyleContainer: false
    },
    from: {
      color: paintColor,
      width: 4
    },
    to: {
      color: paintColor,
      width: 4
    },
    // Set default step function for all animate calls
    step: function(state, circle) {
      circle.path.setAttribute('stroke', state.color);
      circle.path.setAttribute('stroke-width', state.width);

      var value = Math.round(circle.value() * 100) + '%';
      if (value === 0) {
        circle.setText('');
      } else {
        circle.setText(value);
      }

    }
  });
  bar.text.style.fontSize = '1.6rem';

  bar.animate(fraction);
}
