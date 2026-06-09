<%@ page defaultCodec="html" %>
<style>
    @page {
        margin: .25in;
    }

    .landscape {
        size: landscape;
        width: 26.7cm;
    }

    .portrait {
        size: portrait;
    }

    table {
        border-collapse: collapse;
        page-break-inside: auto;
        -fs-table-paginate: paginate;
        border-spacing: 0;
        margin: 5px;
    }

    thead {
        display: table-header-group;
    }

    tr {
        page-break-inside: avoid;
        page-break-after: auto;
    }

    td {
        vertical-align: top;
    }

    th {
        background-color: lightgrey;
        font-weight: bold;
    }

    body {
        font-size: 11px;
    }

    div.header {
        display: block;
        text-align: center;
        position: running(header);
    }

    @page {
        size: letter;
        background: white;
        @top-center {
            content: element(header)
        }
        @bottom-center {
            content: "Page " counter(page) " of " counter(pages);
            font-size: 12px;
            font-family: sans-serif;
        }
    }

    .small {
        font-size: xx-small;
    }

    .large {
        font-size: larger;
    }

    .line {
        border-bottom: 1px solid black;
    }

    .page-content {
        page-break-after: avoid;
    }

    .page-header {
        page-break-before: avoid;
    }

    .break {
        page-break-after: always;
    }

    .page:before {
        content: counter(page);
    }

    .pagecount:before {
        content: counter(pages);
    }

    body {
        font: 11px "lucida grande", verdana, arial, helvetica, sans-serif;
    }

    table {
        border-collapse: collapse;
        page-break-inside: auto;
    }

    thead {
        display: table-header-group;
    }

    table td, table th {
        padding: 5px;
        border: 1px solid lightgrey;
        vertical-align: middle;
    }

    .no-border-table td, .no-border-table th {
        border: 0 !important;
    }

    .m-0 {
        margin: 0 !important;
    }

    .m-5 {
        margin: 5px !important;
    }

    .b-0 {
        border: 0 !important;
    }

    .b-t0 {
        border-top: 0 !important;
    }

    .b-r0 {
        border-right: 0 !important;
    }

    .b-b0 {
        border-bottom: 0 !important;
    }

    .b-l0 {
        border-left: 0 !important;
    }

    .no-padding {
        padding: 0 !important;
    }

    .w100 {
        width: 100% !important;
    }

    .no-wrap {
        white-space: nowrap;
    }

    .gray-background {
        background-color: #ddd !important;
    }

    .fixed-layout {
        table-layout: fixed;
    }

    .break-word {
        word-wrap: break-word;
    }

    .signature-table {
        width: 100%;
        margin: auto;
        margin-bottom: 20px;
        margin-top: 100px;
    }

    .signature-table tr, .signature-table td {
        border: 0px solid lightgrey;
        border-top: 1px solid lightgrey;
        height: 60px;
        vertical-align: top;
    }

    .top {
        vertical-align: top;
    }

    .bottom {
        vertical-align: bottom;
    }

    .right {
        text-align: right;
    }

    .center {
        text-align: center;
    }

    .left {
        text-align: left;
    }

    @media print {
        .print-button {
            display: none;
        }

        .print-header {
            display: none;
        }
    }

    .canceled {
        text-decoration: line-through;
    }

    #select-orientation {
        height: 25px;
        margin-right: 15px;
    }

    .page-start {
        -fs-page-sequence: start;
        page-break-before: avoid;
    }

    .first-line {
        display: flex;
        justify-content: space-between;
    }
</style>

