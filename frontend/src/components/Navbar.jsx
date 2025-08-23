import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';

const Navbar = () => {
    const [selectedMenu, setSelectedMenu] = useState('home');
    const navigate = useNavigate();
    const location = useLocation();

    const menuItems = [
    { name: 'home', path: '/home', iconGray: 'nav/home.png', iconGreen: 'nav/home-green.png' },
    { name: 'map', path: '/map', iconGray: 'nav/map.png', iconGreen: 'nav/map-green-2.png' },
    { name: 'my', path: '/mypage', iconGray: 'nav/my.png', iconGreen: 'nav/my-green.png' }
  ];

    return (
        <div>
            <nav className="fixed bottom-0 z-50
                  left-1/2 transform -translate-x-1/2
                  w-full max-w-[400px]
                  bg-white p-4">
      <div className="flex justify-around items-center">
        {menuItems.map((item) => {
          // 현재 경로(location.pathname)와 메뉴의 경로가 일치하는지 확인
          const isActive = location.pathname === item.path;

          return (
            <div 
              key={item.name}
              className="flex flex-col items-center cursor-pointer" 
              onClick={() => navigate(item.path)} // 클릭 시 해당 경로로 이동
            >
              <img 
                src={isActive ? item.iconGreen : item.iconGray} 
                alt={item.name} 
                className="w-4 h-4 object-contain"
              />
              <span className={`text-xs mt-1 ${isActive ? 'text-green-500' : 'text-gray-500'}`}>
                {item.name}
              </span>
            </div>
          );
        })}
      </div>
    </nav>
        </div>
    );
};

export default Navbar;