import React, { useState, useEffect } from 'react';
import Maps from '../components/Maps';
import { useNavigate, useLocation } from 'react-router-dom';
import Navbar from '../components/Navbar';

const Mappage = () => {
    const [open, setOpen] = useState(false);
    const [username, setUsername] = useState('');
    const navigate = useNavigate();
    const location = useLocation();
    const newMarker = location.state?.newMarker;

    useEffect(() => {
        const savedUsername = localStorage.getItem('username');
        if (savedUsername) {
            setUsername(savedUsername);
        }
    }, []);

    return (
        <div className='text-white'>
            <div className='bg-[#73C03F]'>
                <div className='flex justify-between mb-4'>
                    <p className='font-bold text-xl p-4 mt-4'>함께줍줍</p>
                    <div className='mr-4 mt-4 ml-auto flex items-center gap-2'>
                        <img src='/account_circle.png' alt='회원' className='w-7'/>
                        {username && <span className='text-sm'>안녕하세요 {username}님!</span>}
                    </div>
                </div>
                <div className='rounded-t-3xl overflow-hidden shadow'>
                <Maps newMarker={newMarker}/>

                {/* 플러스버튼 */}
                <button
                        type='button'
                        aria-label='제보하기'
                        onClick={() => setOpen(v => !v)}
                        className='absolute z-100 bottom-25 right-5 md:right-50 lg:right-153 w-12 place-items-center
               hover:scale-105 active:scale-95 transition'>
                <img src='/plusBtn.svg' alt='btn'></img>
               </button>

                {/* 말풍선 */}
               <div className={`absolute z-50 right-6 top-115 md:right-55 lg:right-153
               transition ${open ? "opacity-100 translate-y-0" : "opacity-0 translate-y-2 pointer-events-none"}`}>
                    <button
                        type='button'
                        onClick={() => {setOpen(false); navigate("/report"); }}
                        className='relative flex items-center gap-3 bg-white text-[#73C03F] border-2 border-[#73C03F] px-4 py-3'>
                            쓰레기 제보하기
                            <img src='/angleCket.png' className='w-5'/>
                    </button>
               </div>
            </div>
            </div>
            <Navbar/>
        </div>
    );
};

export default Mappage;
