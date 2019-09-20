<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
	<g:layoutHead/>
    <style>

        body {
            text-align: left;
            font-family: "Lucida Grande", arial, verdana;
            margin: 0px;
            padding: 0px;
        }

        .footer {
            text-align: center;
            font-size: 11px;
        }
        .header {
            padding: 5px;
            margin: 0px;
            background-color: #${session?.warehouse?.bgColor?:'white'};
            color: #${session?.warehouse?.fgColor?:'black'};
        }
        .header a {
            color: #${session?.warehouse?.fgColor?:'white'};
            text-decoration: none;
        }

        .box {
            border: 1px solid lightgrey;
            margin: 5px;
            background-color: #fcfcfc;
            padding: 0px;
            -moz-border-radius: 3px;
            border-radius: 3px;
        }
            /* TABLES */

        table {
            width: 100%;
            border-collapse: collapse;
        }
        table.stripe tbody > tr:nth-child(even)        { background-color: #f7f7f7; }
        table.stripe tbody > tr:nth-child(odd)        { background-color: #fff;  }
        table.details tbody > tr > td:nth-child(even)        { background-color: #fff; }
        table.details tbody > tr > td:nth-child(odd)        { background-color: #f7f7f7;  }

        tr {
            border: 0;
            height: 2em;
        }
        td, th {
            font: 11px "lucida grande", verdana, arial, helvetica, sans-serif;
            line-height: 12px;
            padding: 6px 6px;
            text-align: left;
            vertical-align: middle;
        }
        th {
            color: #666;
            font-size: 11px;
            font-weight: bold;
            line-height: 17px;
            padding: 2px 6px;
            border-top: 1px solid #f2f2f2;
            background-color: #eee;

        }
        th a:link, th a:visited, th a:hover {
            color: #333;
            display: block;
            font-size: 10px;
            text-decoration: none;
            width: 100%;
        }
        h2 {
            top: 0;
            padding: 0px;
            margin: 0px;
            position: relative;
            padding-left: 10px;
            font-size: 15px;
            font-weight: 400;
            color: #555;
            line-height: 18px;
            text-shadow: 1px 1px 2px rgba(255, 255, 255, 0.5);

            position: relative;
            height: 40px;
            line-height: 40px;
            background: #E9E9E9;
            background: -moz-linear-gradient(top, #fafafa 0%, #e9e9e9 100%);
            /* FF3.6+ */

            background: -webkit-gradient(linear, left top, left bottom, color-stop(0%, #fafafa), color-stop(100%, #e9e9e9));
            /* Chrome,Safari4+ */

            background: -webkit-linear-gradient(top, #fafafa 0%, #e9e9e9 100%);
            /* Chrome10+,Safari5.1+ */

            background: -o-linear-gradient(top, #fafafa 0%, #e9e9e9 100%);
            /* Opera11.10+ */

            background: -ms-linear-gradient(top, #fafafa 0%, #e9e9e9 100%);
            /* IE10+ */

            background: linear-gradient(top, #fafafa 0%, #e9e9e9 100%);
            /* W3C */

            filter: progid:DXImageTransform.Microsoft.gradient(startColorstr='#FAFAFA', endColorstr='#E9E9E9');
            -ms-filter: "progid:DXImageTransform.Microsoft.gradient(startColorstr='#FAFAFA', endColorstr='#E9E9E9')";
            border: 1px solid #D5D5D5;

            -webkit-background-clip: padding-box;
        }
        .prop {
            padding: 5px;
            border-top: 1px dotted #ccc;
        }

        .prop .name {
            text-align: right;
            vertical-align: middle;
            width: 25%;
        }
        .prop .value {
            text-align: left;
            vertical-align: middle;
            width: 75%;
        }


        label { font-weight: bold; color: #666; }
        .footer { color: #ccc;}
        .footer a { color: #ccc; }
        .footer { padding: 10px; }


        .button {
            position: relative;
            overflow: visible;
            display: inline-block;
            padding: 0.5em 1em;
            border: 1px solid #d4d4d4;
            margin: 0;
            text-decoration: none;
            text-align: center;
            text-shadow: 1px 1px 0 #fff;
            font:11px/normal sans-serif;
            color: #333;
            white-space: nowrap;
            cursor: pointer;
            outline: none;
            background-color: #ececec;
            background-image: -webkit-gradient(linear, 0 0, 0 100%, from(#f4f4f4), to(#ececec));
            background-image: -moz-linear-gradient(#f4f4f4, #ececec);
            background-image: -ms-linear-gradient(#f4f4f4, #ececec);
            background-image: -o-linear-gradient(#f4f4f4, #ececec);
            background-image: linear-gradient(#f4f4f4, #ececec);
            background-clip: padding-box;
            border-radius: 0.2em;
            /* IE hacks */
            zoom: 1;
            *display: inline;
        }

        .button:hover,
        .button:focus,
        .button:active,
        .button.active {
            border-color: #3072b3;
            border-bottom-color: #2a65a0;
            text-decoration: none;
            text-shadow: -1px -1px 0 rgba(0,0,0,0.3);
            color: #fff;
            background-color: #3c8dde;
            background-image: -webkit-gradient(linear, 0 0, 0 100%, from(#599bdc), to(#3072b3));
            background-image: -moz-linear-gradient(#599bdc, #3072b3);
            background-image: -o-linear-gradient(#599bdc, #3072b3);
            background-image: linear-gradient(#599bdc, #3072b3);
        }

        .button:active,
        .button.active {
            border-color: #2a65a0;
            border-bottom-color: #3884cd;
            background-color: #3072b3;
            background-image: -webkit-gradient(linear, 0 0, 0 100%, from(#3072b3), to(#599bdc));
            background-image: -moz-linear-gradient(#3072b3, #599bdc);
            background-image: -ms-linear-gradient(#3072b3, #599bdc);
            background-image: -o-linear-gradient(#3072b3, #599bdc);
            background-image: linear-gradient(#3072b3, #599bdc);
        }

            /* overrides extra padding on button elements in Firefox */
        .button::-moz-focus-inner {
            padding: 0;
            border: 0;
        }
        .middle { vertical-align: middle; }
        .top { vertical-align: top; }
        .bottom { vertical-align: bottom; }
        div.left { float: left; width: 48%; }
        div.right { float: right; width: 48%; }
        div.clear { clear: both; }
        .fade { color: #666; }
        blockquote { margin: 5px 5px 5px 20px; }

    </style>
</head>
<body>

    <div class="header">
        <g:render template="/email/header"/>
    </div>

    <g:layoutBody />
    <div class="footer">
        <g:render template="/common/footer"/>
    </div>
</body>
</html>
