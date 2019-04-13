// This is a manifest file that'll be compiled into application.js.
//
// Any JavaScript file within this directory can be referenced here using a relative path.
//
// You're free to add application-wide JavaScript to this file, but it's generally better
// to create separate JavaScript files as needed.
//
//= require sockjs-1.3.0.min
//= require stomp
//= require self

if (typeof jQuery !== 'undefined') {
    (function($) {
      var exchange = $("#feed-watcher").data( "exchange" );
      var topic_pattern = $("#feed-watcher").data( "feedid" );
      console.log("feedid on div %o",topic_pattern);
      console.log("data on body tag %o",$("#feed-watcher").data());
      initEvents(exchange,topic_pattern);
    })(jQuery);
}


function initEvents(exchange,topic_pattern) {

  
  // var stomp_addr = "ws://"+ window.location.hostname + ":15674/stomp"
  // var stomp_addr = "/rabbitws/stomp"
  // var stomp_addr = window.location.protocol + '//' + window.location.hostname + '/rabbitws/stomp';
  // stomp_addr is now set in the application layout.gsp and does something different when env==development
  console.log("Connect to %s",stomp_addr);

  var ws = new SockJS(stomp_addr);
  var client = Stomp.over(ws);

  // SockJS does not support heart-beat: disable heart-beats
  client.heartbeat.incoming = 0;
  client.heartbeat.outgoing = 0;
  
  client.debug = function(e) {
    // console.log("debug %o",e);
    // $('#second div').append($("<code>").text(e));
  };
  
  // default receive callback to get message from temporary queues
  client.onreceive = function(m) {
    // console.log("message %o",m);
    // $('#first div').append($("<code>").text(m.body));
  }
  
  var on_connect = function(x) {
    console.log("Connected - subscribe to /exchange/%s/%s",exchange,topic_pattern);

    var id = client.subscribe("/exchange/"+exchange+"/"+topic_pattern, function(m) {
      // console.log("/exchange/%s/%s Got message %o",exchange,topic_pattern,m);
      capEvent(exchange,topic_pattern,m)
    });
  };

  var on_error =  function() {
    console.log('error');
  };

  // console.log("Connect...");
  client.connect('cap', 'cap', on_connect, on_error, '/');
  // console.log("Connect complete...");
}

