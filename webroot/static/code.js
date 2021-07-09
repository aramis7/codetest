$(document).ready(function () {
    $('#tblServices').DataTable({
        searching: false,
        info: false,
        paging: true,
        "bLengthChange": false,
        "ajax": {"url": "/service", "dataSrc": ""},
        "columns": [
            {data: "alias"},
            {data: "url"},
            {data: "status"},
            {
                data: "modifyDate",
                "render": function (data) {
                    return moment(data).format('DD/MM/YYYY HH:mm');
                }
            },
            {
                data: null,
                render: function (data, type, row) {
                    return '<button class="delBtn" onclick="getOnclick(\'' + data.alias + '\')">Delete</button>';
                }
            }
        ]
    });
    setTimeout(reloadDataTable, 2000);
});

function reloadDataTable() {
    // alert("reload");
    $('#tblServices').DataTable().ajax.reload();
    setTimeout(reloadDataTable, 2000);

}

const saveButton = document.querySelector('#post-service');
saveButton.onclick = evt => {
    let alias = document.querySelector('#alias').value;
    let urlName = document.querySelector('#url-name').value;
    if (!isValidHttpUrl(urlName)) {
        return alert("Please provide a valid URL");
    }
    fetch('/add', {
        method: 'post',
        headers: {
            'Accept': 'application/json, text/plain, */*',
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({alias: alias, url: urlName})
    }).then(res => {
        if (res.statusText == 'OK')
            location.reload()
        else {
            res.text().then(function (text) {
                alert(text);
            });
        }
    });
}

function getOnclick(alias) {
    alert('delete service');
    fetch('/delete', {
        method: 'post',
        headers: {
            'Accept': 'application/json, text/plain, */*',
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({alias: alias})
    }).then(res => location.reload());
    return;
}

function isValidHttpUrl(string) {
    let url;

    try {
        url = new URL(string);
    } catch (_) {
        return false;
    }

    return url.protocol === "http:" || url.protocol === "https:";
}
