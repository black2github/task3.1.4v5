// методы для работы с админкой через rest.

// У слова async один простой смысл: эта функция всегда возвращает промис. Значения других типов оборачиваются
// в завершившийся успешно промис автоматически.

// полезно для тестирования
function getRandomInt(max) {
    return Math.floor(Math.random() * max);
}

///////////////////////////  клиенты REST //////////////////////////

// структура для хранения данных пользователя
// let user = {
//     "id": null,
//     "firstName": "",
//     "lastName": "",
//     "email": "",
//     "password": "",
//     "age": 0,
//     "roles": [ "" ]
// };

// рабочая. Пример работы ниже.
// user.email = getRandomInt(1000) + user.email;
// user.id = 2;
// user.roles[0] = "ADMIN";
// user.roles[2] = "USER";
// user.roles[4] = "ADMIN";
// updateUserById(user).then(a => alert(JSON.stringify(a, null, 2)));
async function updateUserById(user) {
    let response = await fetch('http://localhost:8080/api/admin/v1/users/' + user.id, {
        method: 'PATCH',
        headers: {
            'Content-Type': 'application/json;charset=utf-8'
        },
        body: JSON.stringify(user)
    });
    if (!response.ok) {
        let msg = await response.json();
        throw (msg);
    } else {
        return await response.json();
    }
}

// рабочая
// deleteUser(45).then(a => alert(a));

// async function deleteUser(id) {
//     // для разнообразия и опыта сделано через элемент формы.
//     let formData = new FormData();
//     formData.append('id', id);
//
//     let response = await fetch('http://localhost:8080/api/admin/v1/users', {
//         method: 'DELETE',
//         // правльный 'Content-Type' в этом случае подставляется в headers автоматом,
//         // ничего дополнительно делать не нужно!
//         // headers: {
//         //     'Content-Type': 'form/multipart'
//         // },
//         body: formData
//     });
//     if (!response.ok) {
//         alert("FAIL: " + response.status)
//         let msg = await response.json();
//         alert("deleteUser() error: " + msg.error);
//         return msg.error;
//     } else {
//         return null;
//     }
// }

async function deleteUserById(id) {
    // для разнообразия и опыта сделано через элемент формы.
    // let formData = new FormData();
    // formData.append('id', id);

    let response = await fetch('http://localhost:8080/api/admin/v1/users/' + id, {
        method: 'DELETE'
        // правльный 'Content-Type' в этом случае подставляется в headers автоматом,
        // ничего дополнительно делать не нужно!
        // headers: {
        //     'Content-Type': 'form/multipart'
        // },
        // body: formData
    });
    if (!response.ok) {
        alert("FAIL: " + response.status)
        let msg = await response.json();
        alert("deleteUser() error: " + msg.error);
        return msg.error;
    } else {
        return null;
    }
}

// рабочая
// использование:  listAll().then(a => alert("listAll() ->" + a[1].email));
async function listAll() {
    let response = await fetch('http://localhost:8080/api/admin/v1/users');

    if (response.ok) { // если HTTP-статус в диапазоне 200-299
        return await response.json();
    } else {
        alert("listALL() FAIL: " + response.status + " - " + response.statusText);
        return null;
    }
}

// рабочая
// использование: getUser(1).then(a => alert(a.email));
async function getUser(id) {
    let response = await fetch(`http://localhost:8080/api/admin/v1/users/${id}`);
    if (!response.ok) {
        alert("FAIL: " + response.status)
        let msg = await response.json();
        alert("getUser(): " + msg.error);
        return null;
    } else {
        return await response.json();
    }
}

// рабочая
async function createUser(user) {
    let response = await fetch('http://localhost:8080/api/admin/v1/users', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json;charset=utf-8'
        },
        body: JSON.stringify(user)
    });
    if (!response.ok) {
        // alert("FAIL: " + response.status)
        let msg = await response.json();
        throw (msg);
    } else {
        return await response.json();
    }
}


/////////////////////////////   заполнение данных форм админ панели //////////////////////////////////

//
// Заполняет шаблон HTML таблицы данными пользователей.
// Вызывается при загрузке административной панели.
//
function appendAllUserRows(users) {
    let tbl = document.getElementById('data-kata-tbl-user-list'); // table reference
    let tbd = tbl.getElementsByTagName("tbody")[0];
    for (let i=0; i < users.length; i++) {
        // клонировать первую строку тела таблицы
        let newRow = tbd.rows[0].cloneNode(true);
        // дать строке уникальный идентификатор (для дальнейшего поиска)
        newRow.setAttribute("id", "data-kata-user-id-" + users[i].id);
        // заполнить строку данными пользователя
        fillRow(newRow, users[i]);
        // сделать срокку видимой - убрать style="display: none;"
        newRow.removeAttribute("style");
        // вставить строку перед последней (скрытой/шаблонной) строкой
        tbd.insertBefore(newRow, tbd.rows[i]);
    }
}


//
// заполнение строки таблицы данными пользователя
//
function fillRow(row, user) {
    let tds = row.getElementsByTagName("td");
    // положить в ячейки нужные значения
    tds[0].textContent = user.id; //"id1";
    tds[1].textContent = user.firstName; // "newFirstName";
    tds[2].textContent = user.lastName; //"newLastName";
    tds[3].textContent = user.age; // "newAge";
    tds[4].textContent = user.email; // "newEmail";
    tds[5].textContent = user.roles.join(' '); // "newRoles1 newRoles2" список ролей с именами через пробел
    // кнопка Edit
    let editEl = tds[6].getElementsByTagName("button");
    editEl[0].setAttribute("data-bs-id", user.id);
    editEl[0].setAttribute("data-bs-firstname", user.firstName);
    editEl[0].setAttribute("data-bs-lastname", user.lastName);
    editEl[0].setAttribute("data-bs-age", user.age);
    editEl[0].setAttribute("data-bs-email", user.email);
    editEl[0].setAttribute("data-bs-role", user.roles.join(' '));
    // кнопка Delete
    let delEl = tds[7].getElementsByTagName("button");
    delEl[0].setAttribute("data-bs-id", user.id);
    delEl[0].setAttribute("data-bs-firstname", user.firstName);
    delEl[0].setAttribute("data-bs-lastname", user.lastName);
    delEl[0].setAttribute("data-bs-age", user.age);
    delEl[0].setAttribute("data-bs-email", user.email);
    delEl[0].setAttribute("data-bs-role", user.roles.join(' '));
}

//
// удаление строки с данными о пользователе
//
function removeUserRow(id) {
    // найти строку с уникальным идентификатором
    let row = document.getElementById('data-kata-user-id-' + id);
    // удалить
    row.remove();
}

//
// добавление данных пользователя в конец таблицы
//
function appendUserRow(user) {
    let tbl = document.getElementById('data-kata-tbl-user-list'); // table reference
    let tbd = tbl.getElementsByTagName("tbody")[0];
    //let newRow = tbl.rows[1].cloneNode(true); // клонировать строку таблицы (следующую после заголовка)
    let newRow = tbd.rows[0].cloneNode(true); // клонировать первую строку тела таблицы
    // дать строке уникальный идентификатор (для дальнейшего поиска)
    newRow.setAttribute("id", "data-kata-user-id-" + user.id);
    // заполнить строку данными пользователя
    fillRow(newRow, user);
    // сделать срокку видимой - убрать style="display: none;
    newRow.removeAttribute("style");
    // вставить строку перед последней (скрытой/шаблонной) строкой
    let index = tbd.rows.length;
    tbl.getElementsByTagName("tbody")[0].insertBefore(newRow, tbl.rows[index]);
}

//
// обновление строки таблицы данными пользователя
//
function updateUserRow(user) {
    // найти сроку с идентификатором "data-kata-user-id-" + user.id
    let row = document.getElementById('data-kata-user-id-'+user.id);
    // заменить в строке все значения на полученные
    fillRow(row, user);
}

//
// переключение таба со списка пользователей или добавления
//
function switchTab() {
    // найти элементы с идентификаторами "nav-profile"
    let profile = document.getElementById('nav-profile');
    let home = document.getElementById('nav-home');
    // перенести строку "active show" атрибута class из одного в другой
    let classHomeAtt = home.getAttribute("class");
    let classProfileAtt = profile.getAttribute("class");
    let homeString = classHomeAtt.toString();
    let profileString = classProfileAtt.toString();

    profileString = profileString.replace("active show","").trim();
    homeString = homeString + " " + "active show";

    home.setAttribute("class", homeString);
    profile.setAttribute("class", profileString);
}