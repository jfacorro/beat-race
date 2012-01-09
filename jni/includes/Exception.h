#ifndef _Exception_H_
#define _Exception_H_

class Exception
{
    private:
	const char * message;
    public:

    Exception(const char * message)
    {
		this->message = message;
    }

    const char * getMessage() { return this->message; }
};

#endif
