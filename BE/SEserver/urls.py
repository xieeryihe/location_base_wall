"""SEserver URL Configuration

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/3.2/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  path('', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  path('', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.urls import include, path
    2. Add a URL to urlpatterns:  path('blog/', include('blog.urls'))
"""
from django.contrib import admin
from django.urls import path, re_path
from django.views.static import serve
from SEserver import settings

from . import methods

urlpatterns = [
    path('admin/', admin.site.urls),
    path('hello/', methods.hello),
    path('api/user/register', methods.register),
    path('api/user/login', methods.login),
    path('api/user/edit', methods.edit_user),
    path('api/post', methods.op_post),
    path('api/post/', methods.op_post_with_id),
    path('api/comment', methods.op_comment),
    path('api/subcomment', methods.op_subcomment),
    re_path(r'media/(?P<path>.*)$',serve,{'document_root':settings.MEDIA_ROOT}),
    # path('api/search', methods.search_posts),
    path('api/post/delete', methods.delete_post),
    path('api/comment/delete', methods.delete_comment),
    path('api/subcomment/delete', methods.delete_subcomment),
    # # path('logout/', methods.logout),
    # # path('insert_user/', methods.insert_user),
    # path('insert_project_data/', methods.insert_project_data),
    # path('insert_project_result/', methods.insert_project_result),
    # path('read_user/', methods.read_user),
    # path('read_project_data/', methods.read_project_data),
    # path('read_promotion/', methods.read_promotion),
    # path('check_promotion/', methods.check_promotion),
    # path('modify_authority/', methods.modify_authority),
    # path('get_all_projects/', methods.get_all_projects),
    # path('get_all_users/', methods.get_all_users),
    # path('get_auth/', methods.get_auth),
    # #path('upload_img/', views.upload_img)
]
