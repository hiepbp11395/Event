﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System.Web;
using YTicket.API2.DTO;
using YTicket.API2.Models;
using YTicket.API2.Models.DTO;
using YTicket.API2.Respositories;

namespace YTicket.API2.Services
{
    public class UserService : IUserService
    {
        private IUserRespository _respository;
        private ICategoryRespository _categoryRespository;
        private IValidationDictionary _validationDictionary;
        private IEventRespository _eventRespository;
        private INotificationRespository _notificationRespository;

        private static int TotalResults;

        public UserService(IValidationDictionary validationDictionary, IUserRespository respository,
            ICategoryRespository categoryRespository, IEventRespository eventRespository,
            INotificationRespository notificationRespository)
        {
            _respository = respository;
            _categoryRespository = categoryRespository;
            _validationDictionary = validationDictionary;
            _eventRespository = eventRespository;
            _notificationRespository = notificationRespository;
        }

        public IEnumerable<UserDTO> GetAllPaging(int pageNumber, int pageSize)
        {
            var list = _respository.GetAllPaging(pageNumber, pageSize);
            TotalResults = _respository.GetTotalResults();
            return list;
        }

        public IEnumerable<UserDTO> GetByNamePaging(string name, int pageNumber, int pageSize)
        {
            var list = _respository.GetByNamePaging(name, pageNumber, pageSize);
            TotalResults = _respository.GetTotalResults();
            return list;
        }

        public UserDetailDTO GetUserDetail(int id)
        {
            var user = _respository.Get(id);
            if (user == null)
            {
                return null;
            }
            else
            {
                return new UserDetailDTO
                {
                    ID = user.ID,
                    Username = user.Username,
                    Address = user.Address,
                    Email = user.Email,
                    Image = user.Image,
                    Phone = user.Phone,
                    Categories = user.Categories.ToList()
                };
            }
        }

        public async Task<UserDetailDTO> GetUserDetailAsync(int id)
        {
            var user = await _respository.GetAsync(id);
            if (user == null)
            {
                return null;
            }
            else
            {
                return new UserDetailDTO
                {
                    ID = user.ID,
                    Username = user.Username,
                    Address = user.Address,
                    Email = user.Email,
                    Image = user.Image,
                    Phone = user.Phone,
                    Categories = user.Categories.ToList()
                };
            }
        }

        protected bool ValidateUser(User user)
        {
            // Validate Address
            if (user.Address.Trim().Length > 50)
                _validationDictionary.AddErrors("Address", "Address cannot exceeds 50 characters.");
            // Validate Phone
            //Todo
            // Validate Image
            //Todo
            // Validate Categories
            foreach (var item in user.Categories)
            {
                var category = _categoryRespository.Get(item.ID);
                if (category == null)
                {
                    _validationDictionary.AddErrors("Categories", "Category is not valid");
                    break;
                }
            }

            return _validationDictionary.IsValid;
        }

        public bool UpdateUser(User user, string username)
        {
            // Validate not found
            var u = _respository.Get(user.ID);
            if (u == null)
            {
                _validationDictionary.AddErrors("User", "Not found");
                return false;
            }

            // Validate authorization
            if (!u.Username.Trim().Equals(username.Trim()))
            {
                _validationDictionary.AddErrors("Authorization", "User does not have permission.");
                return false;
            }

            // Validate logic
            if (!ValidateUser(user))
                return false;

            // Database logic
            try
            {
                _respository.UpdateUser(user);
            }
            catch
            {
                return false;
            }
            return true;
        }

        public async Task<bool> UpdateUserAsync(User user, string username)
        {
            // Validate not found
            var u = await _respository.GetAsync(user.ID);
            if (u == null)
            {
                _validationDictionary.AddErrors("User", "Not found");
                return false;
            }

            // Validate authorization
            if (!u.Username.Trim().Equals(username.Trim()))
            {
                _validationDictionary.AddErrors("Authorization", "User does not have permission.");
                return false;
            }

            // Validate logic
            if (!ValidateUser(user))
                return false;

            // Database logic
            try
            {
                await _respository.UpdateUserAsync(user);
            }
            catch
            {
                return false;
            }
            return true;
        }

        public int GetTotalResults()
        {
            return TotalResults;
        }

        public UserDTO GetCurrentUser(string name)
        {
            User user = _respository.GetByUsername(name);
            if (user == null)
                return null;
            else
                return new UserDTO
                {
                    Username = user.Username,
                    ID = user.ID,
                    Image = user.Image
                };
        }

        public async Task<UserDTO> GetCurrentUserAsync(string name)
        {
            User user = await _respository.GetByUsernameAsync(name);
            if (user == null)
                return null;
            else
                return new UserDTO
                {
                    Username = user.Username,
                    ID = user.ID,
                    Image = user.Image
                };
        }

        public UserDetailDTO GetCurrentUserDetail(string name)
        {
            User user = _respository.GetByUsername(name);
            if (user == null)
                return null;
            else
                return new UserDetailDTO
                {
                    ID = user.ID,
                    Username = user.Username,
                    Email = user.Email,
                    Address = user.Address,
                    Categories = user.Categories.ToList(),
                    Phone = user.Phone,
                    Image = user.Image
                };
        }

        public async Task<UserDetailDTO> GetCurrentUserDetailAsync(string name)
        {
            User user = await _respository.GetByUsernameAsync(name);
            if (user == null)
                return null;
            else
                return new UserDetailDTO
                {
                    ID = user.ID,
                    Username = user.Username,
                    Email = user.Email,
                    Address = user.Address,
                    Categories = user.Categories.ToList(),
                    Phone = user.Phone,
                    Image = user.Image
                };
        }

        public UserDTO GetUserByEvent(int eventId)
        {
            var @event = _eventRespository.Get(eventId);
            if (@event == null)
            {
                return null;
            }
            else
            {
                var user = _eventRespository.GetMaster(@event);
                if (user == null)
                    return null;
                else
                    return new UserDTO
                    {
                        ID = user.ID,
                        Username = user.Username,
                        Image = user.Image
                    };
            }
        }

        public async Task<UserDTO> GetUserByEventAsync(int eventId)
        {
            var @event = await _eventRespository.GetAsync(eventId);
            if (@event == null)
            {
                return null;
            }
            else
            {
                var user = await _eventRespository.GetMasterAsync(@event);
                if (user == null)
                    return null;
                else
                    return new UserDTO
                    {
                        ID = user.ID,
                        Username = user.Username,
                        Image = user.Image
                    };
            }
        }

        public IEnumerable<NotificationDTO> GetNotificationCurrentUser(string username)
        {
            var user = _respository.GetByUsername(username);
            if (user == null)
                return null;

            var list = _notificationRespository.GetAllByUser(user);
            return list;
        }
    }

    public interface IUserService
    {
        IEnumerable<UserDTO> GetAllPaging(int pageNumber, int pageSize);
        IEnumerable<UserDTO> GetByNamePaging(string name, int pageNumber, int pageSize);
        IEnumerable<NotificationDTO> GetNotificationCurrentUser(string username);
        UserDetailDTO GetUserDetail(int id);
        Task<UserDetailDTO> GetUserDetailAsync(int id);
        bool UpdateUser(User user, string username);
        Task<bool> UpdateUserAsync(User user, string username);
        UserDTO GetCurrentUser(string name);
        Task<UserDTO> GetCurrentUserAsync(string name);
        UserDetailDTO GetCurrentUserDetail(string name);
        Task<UserDetailDTO> GetCurrentUserDetailAsync(string name);
        UserDTO GetUserByEvent(int eventId);
        Task<UserDTO> GetUserByEventAsync(int eventId);
        int GetTotalResults();
    }
}