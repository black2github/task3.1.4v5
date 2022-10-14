// с ошибкой по LAZY
select
    u.id as id,
    u.email as email,

from
    users u
where
    u.id=?

///////// без сообщение об ошибке
select 
    u.id as uid, 
    r.id as rid, 
    u.email as uemail,

    r.name as rname,
    u_r.user_id,
    u_r.roles_id
from 
    users u 
        left outer join users_roles u_r on u.id=u_r.user_id
                        left outer join roles r on u_r.roles_id=r.id
where u.id=?

////// join fetch o.roles //// 

select 
    u.id as uid,
    r.id as rid,
    u.email as uemail,
    r.name as rname,
    u_r.user_id,
    u_r.roles_id
from 
    users u inner join users_roles u_r on u.id=u_r.user_id
                        inner join  roles r on u_r.roles_id=r.id
where u.id=?
