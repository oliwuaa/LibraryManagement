import React, {useEffect, useState, useRef} from 'react';
import Select from 'react-select';
import Navbar from '../components/Navbar';
import '../styles/ManageResources.css';
import '../styles/ReservationPage.css';
import {fetchWithAuth} from '../Api.js';
import GlobalAlert from "../components/GlobalAlert.jsx";

const roleOptions = [
    {value: 'ADMIN', label: 'Admin'},
    {value: 'LIBRARIAN', label: 'Librarian'},
    {value: 'USER', label: 'User'},
];

const ManageUsersPage = () => {
    const [users, setUsers] = useState([]);
    const [filteredUsers, setFilteredUsers] = useState([]);
    const [roleFilter, setRoleFilter] = useState('ALL');
    const [searchEmail, setSearchEmail] = useState('');
    const [searchName, setSearchName] = useState('');
    const [editingUserId, setEditingUserId] = useState(null);
    const [editFormData, setEditFormData] = useState({name: '', email: '', role: ''});
    const [libraries, setLibraries] = useState([]);
    const [alertMsg, setAlertMsg] = useState('');
    const [alertType, setAlertType] = useState('info');

    const debounceTimeout = useRef(null);
    const userBeingEdited = users.find(u => u.id === editingUserId);
    const usersToRender = editingUserId && userBeingEdited ? [userBeingEdited] : filteredUsers;

    useEffect(() => {
        fetchUsers();
        fetchLibraries();
    }, []);

    const fetchUsers = async () => {
        const res = await fetchWithAuth('/users/active');
        if (res?.ok) {
            const data = await res.json();
            setUsers(data);
            setFilteredUsers(data);
        }
    };

    const fetchLibraries = async () => {
        const res = await fetchWithAuth('/libraries');
        if (res?.ok) {
            const data = await res.json();
            setLibraries(data);
        }
    };

    useEffect(() => {
        if (debounceTimeout.current) {
            clearTimeout(debounceTimeout.current);
        }
        debounceTimeout.current = setTimeout(() => {
            let filtered = [...users];
            if (roleFilter !== 'ALL') {
                filtered = filtered.filter(u => u.role === roleFilter);
            }
            if (searchEmail.trim()) {
                filtered = filtered.filter(u => u.email.toLowerCase().includes(searchEmail.toLowerCase()));
            }
            if (searchName.trim()) {
                filtered = filtered.filter(u => u.name.toLowerCase().includes(searchName.toLowerCase()));
            }
            setFilteredUsers(filtered);
        }, 300);
    }, [roleFilter, searchEmail, searchName, users]);

    const handleEdit = (user) => {
        setEditingUserId(user.id);
        setEditFormData({
            name: user.name || '',
            email: user.email,
            role: user.role,
            libraryId: user.libraryId || null
        });
    };

    const handleEditChange = (field, value) => {
        setEditFormData(prev => ({...prev, [field]: value}));
    };

    const handleSaveEdit = async () => {
        setAlertType('');
        setAlertMsg('');
        try {
            const userDataToSend = {
                name: editFormData.name,
                email: editFormData.email,
                role: editFormData.role,
                ...(editFormData.role === 'LIBRARIAN' && {libraryId: editFormData.libraryId})
            };

            const res = await fetchWithAuth(`/users/${editingUserId}`, {
                method: 'PUT',
                body: JSON.stringify(userDataToSend)
            });

            if (res?.ok) {
                setUsers(prevUsers => prevUsers.map(u =>
                    u.id === editingUserId ? {...u, ...userDataToSend} : u
                ));
                setEditingUserId(null);
                setAlertType('success');
                setAlertMsg(`Changes saved successfully!`);
            } else {
                setAlertType('error');
                setAlertMsg('Failed to update user.');
            }
        } catch (err) {
            console.error(err);
            setAlertType('error');
            setAlertMsg('Error updating user.');
        }
    };

    const handleDelete = async (id) => {
        if (!window.confirm('Are you sure you want to delete this user?')) return;
        setAlertType('');
        setAlertMsg('');
        try {
            const res = await fetchWithAuth(`/users/${id}`, {
                method: 'DELETE',
            });

            if (res?.ok) {
                setUsers(users.filter(u => u.id !== id));
                setAlertType('success');
                setAlertMsg(`User deleted successfully!`);
            } else {
                setAlertType('error');
                setAlertMsg('Failed to delete user.');
            }
        } catch (err) {
            setAlertType('error');
            setAlertMsg('Error deleting user.')
            console.error(err);
        }
    };

    const userOptions = [...users]
        .sort((a, b) => a.email.localeCompare(b.email))
        .map(u => ({
            value: u.email,
            label: u.email
        }));

    const customSelectStyles = {
        option: (base, state) => ({
            ...base,
            backgroundColor: state.isSelected ? '#4b5563' : state.isFocused ? '#374151' : '#1f2937',
            color: 'white',
            cursor: 'pointer',
        }),
        control: (base, state) => ({
            ...base,
            backgroundColor: '#374151',
            borderColor: '#4b5563',
            boxShadow: state.isFocused ? '0 0 0 1px #6b7280' : 'none',
            '&:hover': {
                borderColor: '#6b7280',
            },
        }),
        menu: (base) => ({
            ...base,
            backgroundColor: '#1f2937',
            color: 'white',
        }),
        singleValue: (base) => ({
            ...base,
            color: 'white',
        }),
        placeholder: (base) => ({
            ...base,
            color: '#9ca3af',
        }),
        input: (base) => ({
            ...base,
            color: 'white',
        }),
    };

    return (
        <div className="manage-layout">
            <Navbar/>
            <GlobalAlert message={alertMsg} type={alertType} onClose={() => setAlertMsg('')}/>
            <div className="resource-container">
                <h2>Manage Users</h2>
                <div className="search-section">
                    <div className="filter-container" style={{marginBottom: '1rem'}}>
                        <label>Filter by Role:</label>
                        <Select
                            options={[{value: 'ALL', label: 'All'}, ...roleOptions]}
                            value={[{value: 'ALL', label: 'All'}, ...roleOptions].find(opt => opt.value === roleFilter)}
                            onChange={selected => setRoleFilter(selected.value)}
                            styles={customSelectStyles}
                            placeholder="Select role..."
                            isSearchable={false}
                        />
                    </div>

                    <div className="filter-container">
                        <label>Search by User Email:</label>
                        <Select
                            options={userOptions}
                            value={searchEmail ? {label: searchEmail, value: searchEmail} : null}
                            onChange={(selected) => setSearchEmail(selected ? selected.value : '')}
                            onInputChange={(inputValue) => setSearchEmail(inputValue)}
                            styles={customSelectStyles}
                            placeholder="Search email..."
                            isClearable
                        />
                    </div>
                </div>

                <div className="book-list-section">
                    {usersToRender.length === 0 ? (
                        <p>No users found.</p>
                    ) : (
                        <div className="book-list-grid">
                            {usersToRender.map(user => (
                                <div key={user.id} className="book-holder">
                                    <div className="book-header" style={{cursor: 'default'}}>
                                        <div className="book-info">
                                            <h4>User #{user.id}</h4>
                                            {editingUserId === user.id ? (
                                                <>
                                                    <p><strong>Name:</strong>{' '}
                                                        <input
                                                            value={editFormData.name}
                                                            onChange={e => handleEditChange('name', e.target.value)}
                                                        />
                                                    </p>
                                                    <p><strong>Email:</strong>{' '}
                                                        <input
                                                            value={editFormData.email}
                                                            onChange={e => handleEditChange('email', e.target.value)}
                                                        />
                                                    </p>
                                                    <p><strong>Role:</strong>{' '}
                                                        <Select
                                                            options={roleOptions}
                                                            value={roleOptions.find(opt => opt.value === editFormData.role)}
                                                            onChange={selected => handleEditChange('role', selected.value)}
                                                            styles={customSelectStyles}
                                                            isSearchable={false}
                                                        />
                                                    </p>
                                                    {editFormData.role === 'LIBRARIAN' && (
                                                        <p><strong>Library:</strong>{' '}
                                                            <Select
                                                                options={libraries.map(lib => ({
                                                                    value: lib.id,
                                                                    label: lib.name
                                                                }))}
                                                                value={libraries.find(lib => lib.id === editFormData.libraryId)
                                                                    ? {
                                                                        value: editFormData.libraryId,
                                                                        label: libraries.find(lib => lib.id === editFormData.libraryId).name
                                                                    }
                                                                    : null}
                                                                onChange={selected => handleEditChange('libraryId', selected.value)}
                                                                styles={customSelectStyles}
                                                                placeholder="Select library"
                                                                isSearchable
                                                            />
                                                        </p>
                                                    )}
                                                </>
                                            ) : (
                                                <>
                                                    <p><strong>Name:</strong> {user.name || '-'}</p>
                                                    <p><strong>Email:</strong> {user.email}</p>
                                                    <p><strong>Role:</strong> {user.role}</p>
                                                </>
                                            )}
                                        </div>
                                        <div className="book-actions">
                                            {editingUserId === user.id ? (
                                                <>
                                                    <button className="add-copy-inline-btn"
                                                            onClick={handleSaveEdit}>Save
                                                    </button>
                                                    <button className="delete-btn"
                                                            onClick={() => setEditingUserId(null)}>Cancel
                                                    </button>
                                                </>
                                            ) : (
                                                <>
                                                    <button className="add-copy-inline-btn"
                                                            onClick={() => handleEdit(user)}>Edit
                                                    </button>
                                                    <button className="delete-btn"
                                                            onClick={() => handleDelete(user.id)}>Delete
                                                    </button>
                                                </>
                                            )}
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default ManageUsersPage;